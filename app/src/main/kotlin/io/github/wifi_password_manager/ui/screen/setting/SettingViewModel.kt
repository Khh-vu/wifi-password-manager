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
import io.github.wifi_password_manager.utils.groupBySsidAndPassword
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.AEADBadTagException
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
        val showImportModeDialog: Boolean = false,
        val showImportPasswordDialog: Boolean = false,
        val mode: PrivilegedMode = PrivilegedMode.NONE,
    )

    sealed interface Action {
        data class UpdateLanguage(val language: Settings.Language) : Action

        data class UpdateThemeMode(val themeMode: Settings.ThemeMode) : Action

        data class ToggleMaterialYou(val value: Boolean) : Action

        data class TogglePureBlackTheme(val value: Boolean) : Action

        data class ToggleSearchBarAtBottom(val value: Boolean) : Action

        data class ToggleAppLock(val value: Boolean) : Action

        data class ToggleSecureScreen(val value: Boolean) : Action

        data class TogglePlaintextPasswords(val value: Boolean) : Action

        data class ToggleAutoPersistEphemeralNetworks(val value: Boolean) : Action

        data class ToggleAutoSyncNetworks(val value: Boolean) : Action

        data class ToggleAllowCacheMode(val value: Boolean) : Action

        data class ToggleAllowInsecureReceiver(val value: Boolean) : Action

        data object ImportNetworks : Action

        data class ChooseImportMode(val toDevice: Boolean) : Action

        data object HideImportDialog : Action

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

        data class UpdateThemeMode(val themeMode: Settings.ThemeMode) : Event
    }

    private val _isLoading = MutableStateFlow(false)
    private val _showForgetAllDialog = MutableStateFlow(false)
    private val _showExportDialog = MutableStateFlow(false)

    private enum class ImportDialog {
        MODE,
        PASSWORD,
    }

    private val _importDialog = MutableStateFlow<ImportDialog?>(null)

    val state =
        combine(
                _isLoading,
                _showForgetAllDialog,
                _showExportDialog,
                _importDialog,
                privilegedManager.mode,
            ) { isLoading, showForgetAllDialog, showExportDialog, importDialog, mode ->
                State(
                    isLoading = isLoading,
                    showForgetAllDialog = showForgetAllDialog,
                    showExportDialog = showExportDialog,
                    showImportModeDialog = importDialog == ImportDialog.MODE,
                    showImportPasswordDialog = importDialog == ImportDialog.PASSWORD,
                    mode = mode,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5.seconds),
                initialValue = State(),
            )

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    @Volatile private var pendingImportFiles: List<PlatformFile> = emptyList()
    @Volatile private var pendingImportToDevice = false

    fun onAction(action: Action) {
        when (action) {
            is Action.UpdateLanguage -> updateSettings { it.copy(language = action.language) }
            is Action.UpdateThemeMode -> onUpdateThemeMode(action.themeMode)
            is Action.ToggleMaterialYou -> updateSettings { it.copy(useMaterialYou = action.value) }
            is Action.TogglePureBlackTheme ->
                updateSettings { it.copy(pureBlackTheme = action.value) }

            is Action.ToggleSearchBarAtBottom ->
                updateSettings { it.copy(searchBarAtBottom = action.value) }

            is Action.ToggleAppLock -> updateSettings { it.copy(appLockEnabled = action.value) }
            is Action.ToggleSecureScreen ->
                updateSettings {
                    it.copy(secureScreenEnabled = action.value, plaintextPasswords = false)
                }

            is Action.TogglePlaintextPasswords ->
                updateSettings { it.copy(plaintextPasswords = action.value) }

            is Action.ToggleAutoPersistEphemeralNetworks ->
                updateSettings { it.copy(autoPersistEphemeralNetworks = action.value) }

            is Action.ToggleAutoSyncNetworks ->
                updateSettings { it.copy(autoSyncNetworks = action.value) }

            is Action.ToggleAllowCacheMode ->
                updateSettings { it.copy(allowCacheMode = action.value) }

            is Action.ToggleAllowInsecureReceiver ->
                updateSettings { it.copy(allowInsecureReceiver = action.value) }

            is Action.ImportNetworks -> onImportNetworks()
            is Action.ChooseImportMode -> onChooseImportMode(action.toDevice)
            is Action.HideImportDialog -> onHideImportDialog()
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

    private fun onUpdateThemeMode(themeMode: Settings.ThemeMode) {
        updateSettings { it.copy(themeMode = themeMode) }
        viewModelScope.launch { _event.send(Event.UpdateThemeMode(themeMode)) }
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
            val file =
                FileKit.openFileSaver(
                    suggestedName = "WiFi_${LocalDateTime.now().format(formatter)}",
                    defaultExtension = option.getFileExtension(password),
                ) ?: return@launch

            runCatching {
                    Dispatchers.IO {
                        val networks = wifiRepository.getAllNetworksList().groupBySsidAndPassword()
                        val data =
                            when (option) {
                                ExportOption.PLAIN ->
                                    fileRepository.networksToJson(networks).toByteArray()

                                ExportOption.COMPRESSED -> fileRepository.networksToGZip(networks)
                            }
                        file.write(
                            if (isEncrypted) Crypto.encrypt(data, password, option) else data,
                        )
                    }
                }
                .fold(
                    onSuccess = {
                        _event.send(
                            Event.ShowMessage(
                                UiText.StringResource(R.string.export_networks_success),
                            ),
                        )
                    },
                    onFailure = {
                        Log.e(TAG, "Error exporting networks", it)
                        _event.send(
                            Event.ShowMessage(
                                UiText.StringResource(R.string.export_networks_failed),
                            ),
                        )
                        if (file.exists()) file.delete()
                    },
                )
        }
    }

    private fun onImportNetworks() {
        if (state.value.mode.hasPrivilegedAccess) {
            _importDialog.update { ImportDialog.MODE }
        } else {
            pickAndImport(toDevice = false)
        }
    }

    private fun onChooseImportMode(toDevice: Boolean) {
        _importDialog.update { null }
        pickAndImport(toDevice)
    }

    private fun pickAndImport(toDevice: Boolean) {
        viewModelScope.launch {
            val files =
                FileKit.openFilePicker(
                        mode = FileKitMode.Multiple(),
                        type = FileKitType.File("json", "gz", "bin"),
                    )
                    ?.takeIf { it.isNotEmpty() } ?: return@launch

            val hasEncryptedFiles = files.any { it.extension == "bin" }
            if (hasEncryptedFiles) {
                pendingImportFiles = files
                pendingImportToDevice = toDevice
                _importDialog.update { ImportDialog.PASSWORD }
                return@launch
            }

            performImport(files, password = null, toDevice = toDevice)
        }
    }

    private fun onHideImportDialog() {
        pendingImportFiles = emptyList()
        _importDialog.update { null }
    }

    private fun onConfirmImportWithPassword(password: String) {
        val files = pendingImportFiles
        pendingImportFiles = emptyList()
        _importDialog.update { null }
        performImport(files, password, pendingImportToDevice)
    }

    private fun performImport(files: List<PlatformFile>, password: String?, toDevice: Boolean) {
        viewModelScope.launch {
            _isLoading.update { true }

            try {
                val networks = Dispatchers.IO { parseNetworksFromFiles(files, password) }
                val imported = Dispatchers.IO { importNetworks(networks, toDevice) }
                _event.send(
                    Event.ShowMessage(
                        UiText.StringResource(
                            if (imported) R.string.import_networks_success
                            else R.string.no_network_to_import,
                        ),
                    ),
                )
            } catch (e: SerializationException) {
                Log.w(TAG, "Error parsing JSON", e)
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.invalid_json)))
            } catch (e: AEADBadTagException) {
                Log.w(TAG, "Decryption failed - wrong password or malformed", e)
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.wrong_password)))
            } catch (e: Throwable) {
                Log.e(TAG, "Error importing networks", e)
                _event.send(
                    Event.ShowMessage(UiText.StringResource(R.string.import_networks_failed)),
                )
            } finally {
                wifiRepository.refresh()
                _isLoading.update { false }
            }
        }
    }

    private suspend fun parseNetworksFromFiles(
        files: List<PlatformFile>,
        password: String?,
    ): List<WifiNetwork> {
        if (files.size == 1) return parseNetworksFromFile(files.first(), password)
        return files.flatMap { file ->
            runCatching { parseNetworksFromFile(file, password) }
                .onFailure { Log.e(TAG, "Error parsing file: ${file.name}", it) }
                .getOrDefault(emptyList())
        }
    }

    private suspend fun importNetworks(networks: List<WifiNetwork>, toDevice: Boolean): Boolean {
        val cards =
            networks
                .groupBy { it.ssid to it.password }
                .values
                .map { duplicateNetworks ->
                    val notes =
                        duplicateNetworks
                            .mapNotNull { it.note?.trim() }
                            .filter { it.isNotBlank() }
                            .distinct()
                    duplicateNetworks
                        .first()
                        .copy(
                            securityType = duplicateNetworks.flatMap { it.securityType }.toSet(),
                            note = notes.joinToString("\n").ifEmpty { null },
                        )
                }
        if (cards.isEmpty()) return false

        cards.forEach { wifiRepository.addManual(it) }
        if (toDevice) {
            wifiRepository.saveToSystem(cards.groupBy { it.ssid }.values.map { it.first() })
        }
        cards
            .filter { it.note != null }
            .forEach { wifiRepository.updateNote(it.ssid, it.password, it.note) }
        return true
    }

    private suspend fun parseNetworksFromFile(
        file: PlatformFile,
        password: String?,
    ): List<WifiNetwork> {
        val data = file.readBytes()
        val list =
            when (file.extension) {
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
                val securityType =
                    network.securityType.filterNot {
                        it in WifiNetwork.SecurityType.withoutPassword
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
                        validNetworks
                            .map { async { wifiRepository.forget(it.networkId) } }
                            .awaitAll()
                        if (clearCache) wifiRepository.clearCache()
                    }
                }
                .fold(
                    onSuccess = {
                        wifiRepository.refresh()
                        _event.send(
                            Event.ShowMessage(UiText.StringResource(R.string.forget_success)),
                        )
                    },
                    onFailure = {
                        Log.e(TAG, "Failed to remove networks", it)
                        _event.send(
                            Event.ShowMessage(UiText.StringResource(R.string.forget_failed)),
                        )
                    },
                )

            _isLoading.update { false }
        }
    }
}
