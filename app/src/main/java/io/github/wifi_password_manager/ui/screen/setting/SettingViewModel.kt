@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.ui.screen.setting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.Settings
import io.github.wifi_password_manager.domain.repository.FileRepository
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.utils.UiText
import io.github.wifi_password_manager.utils.groupAndSortedBySsid
import io.github.wifi_password_manager.utils.toWifiConfigurations
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SettingViewModel(
    private val settingRepository: SettingRepository,
    private val wifiRepository: WifiRepository,
    private val fileRepository: FileRepository,
) : ViewModel() {
    companion object {
        private const val TAG = "SettingViewModel"
    }

    data class State(
        val settings: Settings = Settings(),
        val isLoading: Boolean = false,
        val showForgetAllDialog: Boolean = false,
    )

    sealed interface Action {
        data class UpdateLanguage(val language: Settings.Language) : Action

        data class UpdateThemeMode(val themeMode: Settings.ThemeMode) : Action

        data class ToggleMaterialYou(val value: Boolean) : Action

        data class ToggleAppLock(val value: Boolean) : Action

        data class ToggleSecureScreen(val value: Boolean) : Action

        data class ToggleAutoPersistEphemeralNetworks(val value: Boolean) : Action

        data object ImportNetworks : Action

        data object ExportNetworks : Action

        data object ShowForgetAllDialog : Action

        data object HideForgetAllDialog : Action

        data object ConfirmForgetAllNetworks : Action
    }

    sealed interface Event {
        data class ShowMessage(val message: UiText) : Event
    }

    private val _state = MutableStateFlow(State())
    val state =
        combine(_state, settingRepository.settings) { state, settings ->
                state.copy(settings = settings)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5.seconds),
                initialValue = State(),
            )

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    fun onAction(action: Action) {
        Log.d(TAG, "onAction: $action")
        when (action) {
            is Action.UpdateLanguage -> onUpdateLanguage(action.language)
            is Action.UpdateThemeMode -> onUpdateThemeMode(action.themeMode)
            is Action.ToggleMaterialYou -> onToggleMaterialYou(action.value)
            is Action.ToggleAppLock -> onToggleAppLock(action.value)
            is Action.ToggleSecureScreen -> onToggleSecureScreen(action.value)
            is Action.ToggleAutoPersistEphemeralNetworks ->
                onToggleAutoPersistEphemeralNetworks(action.value)

            is Action.ImportNetworks -> onImportNetworks()
            is Action.ExportNetworks -> onExportNetworks()
            is Action.ShowForgetAllDialog -> onShowForgetAllDialog()
            is Action.HideForgetAllDialog -> _state.update { it.copy(showForgetAllDialog = false) }
            is Action.ConfirmForgetAllNetworks -> onForgetAllNetworks()
        }
    }

    private fun onUpdateLanguage(value: Settings.Language) {
        viewModelScope.launch { settingRepository.updateSettings { it.copy(language = value) } }
    }

    private fun onUpdateThemeMode(value: Settings.ThemeMode) {
        viewModelScope.launch { settingRepository.updateSettings { it.copy(themeMode = value) } }
    }

    private fun onToggleMaterialYou(value: Boolean) {
        viewModelScope.launch {
            settingRepository.updateSettings { it.copy(useMaterialYou = value) }
        }
    }

    private fun onToggleAppLock(value: Boolean) {
        viewModelScope.launch {
            settingRepository.updateSettings { it.copy(appLockEnabled = value) }
        }
    }

    private fun onToggleSecureScreen(value: Boolean) {
        viewModelScope.launch {
            settingRepository.updateSettings { it.copy(secureScreenEnabled = value) }
        }
    }

    private fun onToggleAutoPersistEphemeralNetworks(value: Boolean) {
        viewModelScope.launch {
            settingRepository.updateSettings { it.copy(autoPersistEphemeralNetworks = value) }
        }
    }

    private fun onExportNetworks() {
        viewModelScope.launch {
            runCatching {
                    val count = wifiRepository.getNetworkCount()
                    if (count == 0) {
                        _event.send(
                            Event.ShowMessage(UiText.StringResource(R.string.no_network_to_export))
                        )
                        return@launch
                    }

                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")
                    val file =
                        FileKit.openFileSaver(
                            suggestedName = "WiFi_${LocalDateTime.now().format(formatter)}",
                            extension = "json",
                        ) ?: return@launch
                    val networks = wifiRepository.getAllNetworksList().groupAndSortedBySsid()
                    Dispatchers.IO { file.writeString(fileRepository.networksToJson(networks)) }
                }
                .fold(
                    onSuccess = {
                        _event.send(
                            Event.ShowMessage(
                                UiText.StringResource(R.string.export_networks_success)
                            )
                        )
                    },
                    onFailure = {
                        Log.e(TAG, "Error exporting networks", it)
                        _event.send(
                            Event.ShowMessage(
                                UiText.StringResource(R.string.export_networks_failed)
                            )
                        )
                    },
                )
        }
    }

    private fun onImportNetworks() {
        viewModelScope.launch {
            val files =
                FileKit.openFilePicker(
                        mode = FileKitMode.Multiple(),
                        type = FileKitType.File("json"),
                    )
                    ?.takeIf { it.isNotEmpty() } ?: return@launch

            _state.update { it.copy(isLoading = true) }

            try {
                if (files.size == 1) {
                    importSingleFile(files.first())
                } else {
                    importMultipleFiles(files)
                }
                _event.send(
                    Event.ShowMessage(UiText.StringResource(R.string.import_networks_success))
                )
            } catch (e: SerializationException) {
                Log.e(TAG, "Error parsing JSON", e)
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.invalid_json)))
            } catch (e: Throwable) {
                Log.e(TAG, "Error importing networks", e)
                _event.send(
                    Event.ShowMessage(UiText.StringResource(R.string.import_networks_failed))
                )
            } finally {
                wifiRepository.refresh()
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun importSingleFile(file: PlatformFile) =
        Dispatchers.IO {
            val networks = fileRepository.networksFromJson(file.readString())
            if (networks.isEmpty()) {
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.no_network_to_import)))
                return@IO
            }

            networks
                .flatMap { network ->
                    val configs = network.toWifiConfigurations()
                    configs.map { async { wifiRepository.addOrUpdateNetworkPrivileged(it) } }
                }
                .awaitAll()

            wifiRepository.refresh()

            networks
                .filter { it.note != null }
                .map { async { wifiRepository.updateNote(it.ssid, it.note) } }
                .awaitAll()
        }

    private suspend fun importMultipleFiles(files: List<PlatformFile>) {
        Dispatchers.IO {
            val allNetworks =
                files.flatMap { file ->
                    runCatching { fileRepository.networksFromJson(file.readString()) }
                        .onFailure { Log.e(TAG, "Error parsing JSON from file: ${file.name}", it) }
                        .getOrDefault(emptyList())
                }

            val networks =
                allNetworks
                    .groupBy { it.ssid }
                    .values
                    .map { duplicateNetworks ->
                        val network = duplicateNetworks.first()

                        val notes =
                            duplicateNetworks
                                .mapNotNull { it.note?.trim() }
                                .filter { it.isNotBlank() }
                                .distinct()

                        val mergedNote =
                            when {
                                notes.isEmpty() -> null
                                notes.size == 1 -> notes.first()
                                else -> notes.joinToString("\n")
                            }

                        network.copy(note = mergedNote)
                    }

            if (networks.isEmpty()) {
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.no_network_to_import)))
                return@IO
            }

            networks
                .flatMap { network ->
                    val configs = network.toWifiConfigurations()
                    configs.map { async { wifiRepository.addOrUpdateNetworkPrivileged(it) } }
                }
                .awaitAll()

            wifiRepository.refresh()

            networks
                .filter { it.note != null }
                .map { async { wifiRepository.updateNote(it.ssid, it.note) } }
                .awaitAll()
        }
    }

    private fun onShowForgetAllDialog() {
        viewModelScope.launch {
            val count = wifiRepository.getNetworkCount()
            if (count == 0) {
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.no_network_to_forget)))
                return@launch
            }

            _state.update { it.copy(showForgetAllDialog = true) }
        }
    }

    private fun onForgetAllNetworks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showForgetAllDialog = false) }

            runCatching {
                    val networks = wifiRepository.getPrivilegedConfiguredNetworks()
                    val validNetworks = networks.filter { it.networkId != -1 }

                    if (validNetworks.isEmpty()) {
                        Log.d(TAG, "No valid networks to remove")
                        _state.update { it.copy(isLoading = false) }
                        return@launch
                    }

                    Dispatchers.IO {
                        validNetworks
                            .map { async { wifiRepository.removeNetwork(it.networkId) } }
                            .awaitAll()
                    }
                }
                .fold(
                    onSuccess = {
                        wifiRepository.refresh()
                        _event.send(
                            Event.ShowMessage(UiText.StringResource(R.string.forget_success))
                        )
                    },
                    onFailure = {
                        Log.e(TAG, "Failed to remove networks", it)
                        _event.send(
                            Event.ShowMessage(UiText.StringResource(R.string.forget_failed))
                        )
                    },
                )

            _state.update { it.copy(isLoading = false) }
        }
    }
}
