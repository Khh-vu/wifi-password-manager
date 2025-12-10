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
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.repository.FileRepository
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.utils.UiText
import io.github.wifi_password_manager.utils.fromWifiConfiguration
import io.github.wifi_password_manager.utils.toWifiConfigurations
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerializationException

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
        data class UpdateThemeMode(val themeMode: Settings.ThemeMode) : Action

        data class ToggleMaterialYou(val value: Boolean) : Action

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
            is Action.UpdateThemeMode -> onUpdateThemeMode(action.themeMode)
            is Action.ToggleMaterialYou -> onToggleMaterialYou(action.value)
            is Action.ToggleAutoPersistEphemeralNetworks ->
                onToggleAutoPersistEphemeralNetworks(action.value)
            is Action.ImportNetworks -> onImportNetworks()
            is Action.ExportNetworks -> onExportNetworks()
            is Action.ShowForgetAllDialog -> onShowForgetAllDialog()
            is Action.HideForgetAllDialog -> _state.update { it.copy(showForgetAllDialog = false) }
            is Action.ConfirmForgetAllNetworks -> onForgetAllNetworks()
        }
    }

    private fun onUpdateThemeMode(value: Settings.ThemeMode) {
        viewModelScope.launch { settingRepository.updateSettings { it.copy(themeMode = value) } }
    }

    private fun onToggleMaterialYou(value: Boolean) {
        viewModelScope.launch {
            settingRepository.updateSettings { it.copy(useMaterialYou = value) }
        }
    }

    private fun onToggleAutoPersistEphemeralNetworks(value: Boolean) {
        viewModelScope.launch {
            settingRepository.updateSettings { it.copy(autoPersistEphemeralNetworks = value) }
        }
    }

    @OptIn(ExperimentalTime::class, FormatStringsInDatetimeFormats::class)
    private fun onExportNetworks() {
        val configs = wifiRepository.getPrivilegedConfiguredNetworks()
        if (configs.isEmpty()) {
            _event.trySend(Event.ShowMessage(UiText.StringResource(R.string.no_network_to_export)))
            return
        }

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val formatter = LocalDateTime.Format { byUnicodePattern(pattern = "yyyy-MM-dd_HH:mm:ss") }

        runCatching {
                viewModelScope.launch {
                    val file =
                        FileKit.openFileSaver(
                            suggestedName = "WiFi_${formatter.format(now)}",
                            extension = "json",
                        ) ?: return@launch
                    val networks = configs.map(WifiNetwork::fromWifiConfiguration)
                    Dispatchers.IO { file.writeString(fileRepository.networksToJson(networks)) }
                }
            }
            .fold(
                onSuccess = {
                    _event.trySend(
                        Event.ShowMessage(UiText.StringResource(R.string.export_networks_success))
                    )
                },
                onFailure = {
                    Log.e(TAG, "Error exporting networks", it)
                    _event.trySend(
                        Event.ShowMessage(UiText.StringResource(R.string.export_networks_failed))
                    )
                },
            )
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
                _event.trySend(
                    Event.ShowMessage(UiText.StringResource(R.string.import_networks_success))
                )
            } catch (e: SerializationException) {
                Log.e(TAG, "Error parsing JSON", e)
                _event.trySend(Event.ShowMessage(UiText.StringResource(R.string.invalid_json)))
            } catch (e: Throwable) {
                Log.e(TAG, "Error importing networks", e)
                _event.trySend(
                    Event.ShowMessage(UiText.StringResource(R.string.import_networks_failed))
                )
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun importSingleFile(file: PlatformFile) =
        Dispatchers.IO {
            val networks = fileRepository.networksFromJson(file.readString())
            if (networks.isEmpty()) {
                _event.trySend(
                    Event.ShowMessage(UiText.StringResource(R.string.no_network_to_import))
                )
                return@IO
            }

            networks
                .flatMap { network ->
                    val configs = network.toWifiConfigurations()
                    configs.map { async { wifiRepository.addOrUpdateNetworkPrivileged(it) } }
                }
                .awaitAll()
        }

    private suspend fun importMultipleFiles(files: List<PlatformFile>) {
        Dispatchers.IO {
            val networks =
                files
                    .flatMap { file ->
                        runCatching { fileRepository.networksFromJson(file.readString()) }
                            .onFailure {
                                Log.e(TAG, "Error parsing JSON from file: ${file.name}", it)
                            }
                            .getOrDefault(emptyList())
                    }
                    .toSet()

            if (networks.isEmpty()) {
                _event.trySend(
                    Event.ShowMessage(UiText.StringResource(R.string.no_network_to_import))
                )
                return@IO
            }
            networks
                .flatMap { network ->
                    val configs = network.toWifiConfigurations()
                    configs.map { async { wifiRepository.addOrUpdateNetworkPrivileged(it) } }
                }
                .awaitAll()
        }
    }

    private fun onShowForgetAllDialog() {
        val configs = wifiRepository.getPrivilegedConfiguredNetworks()
        if (configs.isEmpty()) {
            _event.trySend(Event.ShowMessage(UiText.StringResource(R.string.no_network_to_forget)))
            return
        }

        _state.update { it.copy(showForgetAllDialog = true) }
    }

    private fun onForgetAllNetworks() {
        val configs = wifiRepository.getPrivilegedConfiguredNetworks()
        val validConfigs = configs.filter { it.networkId != -1 }

        if (validConfigs.isEmpty()) {
            Log.d(TAG, "No valid networks to remove")
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showForgetAllDialog = false) }

            runCatching {
                    Dispatchers.IO {
                        validConfigs
                            .map { async { wifiRepository.removeNetwork(it.networkId) } }
                            .awaitAll()
                    }
                    _state.update { it.copy(isLoading = false) }
                }
                .fold(
                    onSuccess = {
                        _event.trySend(
                            Event.ShowMessage(UiText.StringResource(R.string.forget_success))
                        )
                    },
                    onFailure = {
                        Log.e(TAG, "Failed to remove networks", it)
                        _event.trySend(
                            Event.ShowMessage(UiText.StringResource(R.string.forget_failed))
                        )
                    },
                )
        }
    }
}
