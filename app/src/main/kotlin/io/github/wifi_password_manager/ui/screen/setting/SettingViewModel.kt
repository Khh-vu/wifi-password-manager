package io.github.wifi_password_manager.ui.screen.setting

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.ExportOption
import io.github.wifi_password_manager.domain.model.PrivilegedMode
import io.github.wifi_password_manager.domain.model.Settings
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.repository.FileRepository
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.manager.PrivilegedManager
import io.github.wifi_password_manager.utils.Crypto
import io.github.wifi_password_manager.utils.UiText
import io.github.wifi_password_manager.utils.groupAndSortedBySsid
import io.github.wifi_password_manager.utils.toWifiConfigurations
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
import javax.crypto.AEADBadTagException
import kotlin.time.Duration.Companion.seconds

class SettingViewModel(
    private val settingRepository: SettingRepository,
    private val wifiRepository: WifiRepository,
    private val fileRepository: FileRepository,
    privilegedManager: PrivilegedManager,
) : ViewModel() {
    companion object {
        private const val TAG = "SettingViewModel"
    }

    @Immutable
    data class State(
        val isLoading: Boolean = false,
        val showForgetAllDialog: Boolean = false,
        val showExportDialog: Boolean = false,
        val showImportPasswordDialog: Boolean = false,
        val mode: PrivilegedMode = PrivilegedMode.NONE,
    )

    sealed interface Action {
        data class UpdateLanguage(val language: Settings.Language) : Action

        data class UpdateThemeMode(val themeMode: Settings.ThemeMode) : Action

        data class ToggleMaterialYou(val value: Boolean) : Action

        data class TogglePureBlackTheme(val value: Boolean) : Action

        data class ToggleAppLock(val value: Boolean) : Action

        data class ToggleSecureScreen(val value: Boolean) : Action

        data class TogglePlaintextPasswords(val value: Boolean) : Action

        data class ToggleAutoPersistEphemeralNetworks(val value: Boolean) : Action

        data class ToggleAllowCacheMode(val value: Boolean) : Action

        data class ToggleAllowInsecureReceiver(val value: Boolean) : Action

        data object ImportNetworks : Action

        data object HideImportPasswordDialog : Action

        data class ConfirmImportWithPassword(val password: String) : Action

        data object ShowExportDialog : Action

        data object HideExportDialog : Action

        data class ConfirmExport(val option: ExportOption, val password: String) : Action

        data object ShowForgetAllDialog : Action

        data object HideForgetAllDialog : Action

        data class ConfirmForgetAllNetworks(val clearCache: Boolean) : Action
    }

    sealed interface Event {
        data class ShowMessage(val message: UiText) : Event
    }

    private val _isLoading = MutableStateFlow(false)
    private val _showForgetAllDialog = MutableStateFlow(false)
    private val _showExportDialog = MutableStateFlow(false)
    private val _showImportPasswordDialog = MutableStateFlow(false)

    val state = combine(
        _isLoading,
        _showForgetAllDialog,
        _showExportDialog,
        _showImportPasswordDialog,
        privilegedManager.mode
    ) { isLoading, showForgetAllDialog, showExportDialog, showImportPasswordDialog, mode ->
        State(
            isLoading = isLoading,
            showForgetAllDialog = showForgetAllDialog,
            showExportDialog = showExportDialog,
            showImportPasswordDialog = showImportPasswordDialog,
            mode = mode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = State(),
    )

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    @Volatile
    private var pendingImportFiles: List<PlatformFile> = emptyList()

    fun onAction(action: Action) {
        Log.d(TAG, "onAction: $action")
        when (action) {
            is Action.UpdateLanguage -> updateSettings { it.copy(language = action.language) }
            is Action.UpdateThemeMode -> updateSettings { it.copy(themeMode = action.themeMode) }
            is Action.ToggleMaterialYou -> updateSettings { it.copy(useMaterialYou = action.value) }
            is Action.TogglePureBlackTheme -> updateSettings { it.copy(pureBlackTheme = action.value) }
            is Action.ToggleAppLock -> updateSettings { it.copy(appLockEnabled = action.value) }
            is Action.ToggleSecureScreen -> updateSettings {
                it.copy(secureScreenEnabled = action.value, plaintextPasswords = false)
            }

            is Action.TogglePlaintextPasswords -> updateSettings { it.copy(plaintextPasswords = action.value) }
            is Action.ToggleAutoPersistEphemeralNetworks -> updateSettings {
                it.copy(autoPersistEphemeralNetworks = action.value)
            }

            is Action.ToggleAllowCacheMode -> updateSettings { it.copy(allowCacheMode = action.value) }
            is Action.ToggleAllowInsecureReceiver -> updateSettings { it.copy(allowInsecureReceiver = action.value) }
            is Action.ImportNetworks -> onImportNetworks()
            is Action.HideImportPasswordDialog -> onHideImportPasswordDialog()
            is Action.ConfirmImportWithPassword -> onConfirmImportWithPassword(action.password)
            is Action.ShowExportDialog -> onShowExportDialog()
            is Action.HideExportDialog -> _showExportDialog.update { false }
            is Action.ConfirmExport -> onExportNetworks(action.option, action.password)
            is Action.ShowForgetAllDialog -> onShowForgetAllDialog()
            is Action.HideForgetAllDialog -> _showForgetAllDialog.update { false }
            is Action.ConfirmForgetAllNetworks -> onForgetAllNetworks(action.clearCache)
        }
    }

    private fun updateSettings(block: (Settings) -> Settings) {
        viewModelScope.launch { settingRepository.updateSettings(block) }
    }

    private fun onShowExportDialog() {
        viewModelScope.launch {
            val count = wifiRepository.getNetworkCount()
            if (count == 0) {
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.no_network_to_export)))
                return@launch
            }
            _showExportDialog.update { true }
        }
    }

    private fun onExportNetworks(option: ExportOption, password: String) {
        _showExportDialog.update { false }
        viewModelScope.launch {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")
            val isEncrypted = password.isNotEmpty()
            val file = FileKit.openFileSaver(
                suggestedName = "WiFi_${LocalDateTime.now().format(formatter)}",
                defaultExtension = option.getFileExtension(password),
            ) ?: return@launch

            runCatching {
                Dispatchers.IO {
                    val networks = wifiRepository.getAllNetworksList().groupAndSortedBySsid()
                    val data = when (option) {
                        ExportOption.PLAIN -> fileRepository.networksToJson(networks).toByteArray()
                        ExportOption.COMPRESSED -> fileRepository.networksToGZip(networks)
                    }
                    file.write(if (isEncrypted) Crypto.encrypt(data, password, option) else data)
                }
            }.fold(
                onSuccess = { _event.send(Event.ShowMessage(UiText.StringResource(R.string.export_networks_success))) },
                onFailure = {
                    Log.e(TAG, "Error exporting networks", it)
                    _event.send(Event.ShowMessage(UiText.StringResource(R.string.export_networks_failed)))
                    if (file.exists()) file.delete()
                },
            )
        }
    }

    private fun onImportNetworks() {
        if (!state.value.mode.hasPrivilegedAccess) return
        viewModelScope.launch {
            val files = FileKit.openFilePicker(
                mode = FileKitMode.Multiple(),
                type = FileKitType.File("json", "gz", "bin"),
            )?.takeIf { it.isNotEmpty() } ?: return@launch

            val hasEncryptedFiles = files.any { it.extension == "bin" }
            if (hasEncryptedFiles) {
                pendingImportFiles = files
                _showImportPasswordDialog.update { true }
                return@launch
            }

            performImport(files, password = null)
        }
    }

    private fun onHideImportPasswordDialog() {
        pendingImportFiles = emptyList()
        _showImportPasswordDialog.update { false }
    }

    private fun onConfirmImportWithPassword(password: String) {
        val files = pendingImportFiles
        pendingImportFiles = emptyList()
        _showImportPasswordDialog.update { false }
        performImport(files, password)
    }

    private fun performImport(files: List<PlatformFile>, password: String?) {
        viewModelScope.launch {
            _isLoading.update { true }

            try {
                if (files.size == 1) {
                    importSingleFile(files.first(), password)
                } else {
                    importMultipleFiles(files, password)
                }
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.import_networks_success)))
            } catch (e: SerializationException) {
                Log.e(TAG, "Error parsing JSON", e)
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.invalid_json)))
            } catch (e: AEADBadTagException) {
                Log.e(TAG, "Decryption failed - wrong password or malformed", e)
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.wrong_password)))
            } catch (e: Throwable) {
                Log.e(TAG, "Error importing networks", e)
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.import_networks_failed)))
            } finally {
                wifiRepository.refresh()
                _isLoading.update { false }
            }
        }
    }

    private suspend fun importSingleFile(file: PlatformFile, password: String?) = Dispatchers.IO {
        val networks = parseNetworksFromFile(file, password)
        if (networks.isEmpty()) {
            _event.send(Event.ShowMessage(UiText.StringResource(R.string.no_network_to_import)))
            return@IO
        }

        networks.flatMap { network ->
            val configs = network.toWifiConfigurations()
            configs.map { async { wifiRepository.addOrUpdateNetworkPrivileged(it) } }
        }.awaitAll()

        wifiRepository.refresh()

        networks.filter { it.note != null }
            .map { async { wifiRepository.updateNote(it.ssid, it.note) } }.awaitAll()
    }

    private suspend fun importMultipleFiles(files: List<PlatformFile>, password: String?) {
        Dispatchers.IO {
            val allNetworks = files.flatMap { file ->
                runCatching { parseNetworksFromFile(file, password) }.onFailure {
                    Log.e(TAG, "Error parsing file: ${file.name}", it)
                }.getOrDefault(emptyList())
            }

            val networks = allNetworks.groupBy { it.ssid }.values.map { duplicateNetworks ->
                val network = duplicateNetworks.first()
                val notes =
                    duplicateNetworks.mapNotNull { it.note?.trim() }.filter { it.isNotBlank() }
                        .distinct()
                val mergedNote = when {
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

            networks.flatMap { network ->
                val configs = network.toWifiConfigurations()
                configs.map { async { wifiRepository.addOrUpdateNetworkPrivileged(it) } }
            }.awaitAll()

            wifiRepository.refresh()

            networks.filter { it.note != null }
                .map { async { wifiRepository.updateNote(it.ssid, it.note) } }.awaitAll()
        }
    }

    private suspend fun parseNetworksFromFile(
        file: PlatformFile,
        password: String?,
    ): List<WifiNetwork> {
        val data = file.readBytes()
        val list = when (file.extension) {
            "bin" if password != null -> {
                val (option, decrypted) = Crypto.decrypt(data, password)
                when (option) {
                    ExportOption.PLAIN -> fileRepository.networksFromJson(String(decrypted))
                    ExportOption.COMPRESSED -> fileRepository.networksFromGZip(decrypted)
                }
            }

            "gz" -> fileRepository.networksFromGZip(data)
            else -> fileRepository.networksFromJson(String(data))
        }
        return list.map { network ->
            if (network.password.isNotBlank()) {
                val securityType = network.securityType.filterNot {
                    it in setOf(WifiNetwork.SecurityType.OPEN, WifiNetwork.SecurityType.OWE)
                }
                network.copy(securityType = securityType.toSet())
            } else {
                network
            }
        }
    }

    private fun onShowForgetAllDialog() {
        if (!state.value.mode.hasPrivilegedAccess) return
        viewModelScope.launch {
            val count = wifiRepository.getNetworkCount()
            if (count == 0) {
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.no_network_to_forget)))
                return@launch
            }

            _showForgetAllDialog.update { true }
        }
    }

    private fun onForgetAllNetworks(clearCache: Boolean) {
        viewModelScope.launch {
            _isLoading.update { true }
            _showForgetAllDialog.update { false }

            runCatching {
                val networks = wifiRepository.getPrivilegedConfiguredNetworks()
                val validNetworks = networks.filter { it.networkId != -1 }

                if (validNetworks.isEmpty() && !clearCache) {
                    Log.d(TAG, "No valid networks to remove")
                    _isLoading.update { false }
                    return@launch
                }

                Dispatchers.IO {
                    validNetworks.map { async { wifiRepository.forget(it.networkId) } }.awaitAll()
                    if (clearCache) wifiRepository.clearCache()
                }
            }.fold(
                onSuccess = {
                    wifiRepository.refresh()
                    _event.send(Event.ShowMessage(UiText.StringResource(R.string.forget_success)))
                },
                onFailure = {
                    Log.e(TAG, "Failed to remove networks", it)
                    _event.send(Event.ShowMessage(UiText.StringResource(R.string.forget_failed)))
                },
            )

            _isLoading.update { false }
        }
    }
}
