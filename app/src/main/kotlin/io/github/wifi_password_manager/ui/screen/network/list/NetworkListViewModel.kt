package io.github.wifi_password_manager.ui.screen.network.list

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshotFlow
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
import io.github.vinceglb.filekit.write
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.ExportOption
import io.github.wifi_password_manager.domain.model.PrivilegedMode
import io.github.wifi_password_manager.domain.model.WifiConnectionStatus
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.model.WifiNetwork.SecurityType
import io.github.wifi_password_manager.domain.repository.FileRepository
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.manager.PrivilegedManager
import io.github.wifi_password_manager.utils.UiText
import io.github.wifi_password_manager.utils.groupBySsidAndPassword
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.AEADBadTagException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class NetworkListViewModel(
    private val wifiRepository: WifiRepository,
    private val fileRepository: FileRepository,
    private val privilegedManager: PrivilegedManager,
) : ViewModel() {
    companion object {
        private const val TAG = "NetworkListViewModel"
        private val searchQueryRegex = "[^\\p{L}\\p{N}\\s]".toRegex()
    }

    enum class Filter(
        val group: Group,
        @StringRes val labelResId: Int,
        val matches: (WifiNetwork) -> Boolean,
    ) {
        ON_DEVICE(
            group = Group.LOCATION,
            labelResId = R.string.filter_on_device,
            matches = { it.existInSystem },
        ),
        APP_ONLY(
            group = Group.LOCATION,
            labelResId = R.string.filter_app_only,
            matches = { !it.existInSystem },
        ),
        OPEN(
            group = Group.SECURITY,
            labelResId = R.string.security_open,
            matches = { SecurityType.OPEN in it.securityType },
        ),
        OWE(
            group = Group.SECURITY,
            labelResId = R.string.security_owe,
            matches = { SecurityType.OWE in it.securityType },
        ),
        WPA2(
            group = Group.SECURITY,
            labelResId = R.string.security_wpa2,
            matches = { SecurityType.WPA2 in it.securityType },
        ),
        WPA3(
            group = Group.SECURITY,
            labelResId = R.string.security_wpa3,
            matches = { SecurityType.WPA3 in it.securityType },
        ),
        WEP(
            group = Group.SECURITY,
            labelResId = R.string.security_wep,
            matches = { SecurityType.WEP in it.securityType },
        ),
        HAS_NOTE(
            group = Group.ATTRIBUTES,
            labelResId = R.string.filter_has_note,
            matches = { it.note != null },
        ),
        HIDDEN(
            group = Group.ATTRIBUTES,
            labelResId = R.string.filter_hidden,
            matches = { it.hidden },
        );

        enum class Group(@StringRes val labelResId: Int) {
            LOCATION(R.string.filter_location),
            SECURITY(R.string.security_section),
            ATTRIBUTES(R.string.filter_attributes),
        }
    }

    enum class SortBy(@StringRes val labelResId: Int) {
        SSID(R.string.ssid),
        CREATED(R.string.sort_created),
        UPDATED(R.string.sort_updated),
    }

    @Immutable
    data class State(
        val savedNetworks: List<WifiNetwork> = emptyList(),
        val showingSearch: Boolean = false,
        val connectionStatus: WifiConnectionStatus = WifiConnectionStatus.Disconnected,
        val isCacheMode: Boolean = false,
        val filters: Set<Filter> = emptySet(),
        val sortBy: SortBy = SortBy.SSID,
        val sortAscending: Boolean = true,
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val showForgetAllDialog: Boolean = false,
        val showExportDialog: Boolean = false,
        val showImportModeDialog: Boolean = false,
        val showImportPasswordDialog: Boolean = false,
    )

    sealed interface Action {
        data object Refresh : Action

        data object ToggleSearch : Action

        data class FilterToggled(val filter: Filter) : Action

        data class SortChanged(val sortBy: SortBy) : Action

        data class SortDirectionChanged(val ascending: Boolean) : Action

        data object ResetSortFilter : Action

        data class DeleteNote(val network: WifiNetwork) : Action

        data object Disconnect : Action

        data class Connect(val network: WifiNetwork) : Action

        data class Forget(val network: WifiNetwork) : Action

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
    }

    private val _uiState = MutableStateFlow(State())
    private val _pendingNetwork = MutableStateFlow<WifiNetwork?>(null)
    private val _networks = snapshotFlow {
        searchState.text
    }
        .debounce(200.milliseconds)
        .distinctUntilChanged()
        .flatMapLatest { searchText ->
            val query = searchText.replace(searchQueryRegex, "").trim()
            if (query.isBlank()) {
                wifiRepository.getAllNetworks()
            } else {
                wifiRepository.getAllNetworks(query.lowercase())
            }
        }

    val state =
        combine(
                _uiState,
                _networks,
                privilegedManager.mode,
                wifiRepository.getConnectedNetworkIdFlow().onEach { wifiRepository.refresh() },
                _pendingNetwork,
            ) { ui, networks, mode, connectedNetworkId, pendingNetwork ->
                val sorted =
                    networks
                        .groupBySsidAndPassword()
                        .filter { network ->
                            ui.filters
                                .groupBy { it.group }
                                .all { (group, selected) ->
                                    if (group == Filter.Group.ATTRIBUTES) {
                                        selected.all { it.matches(network) }
                                    } else {
                                        selected.any { it.matches(network) }
                                    }
                                }
                        }
                        .let { list ->
                            when (ui.sortBy) {
                                SortBy.SSID -> list
                                SortBy.CREATED -> list.sortedBy { it.createdAt }
                                SortBy.UPDATED -> list.sortedBy { it.updatedAt }
                            }
                        }
                        .let { if (ui.sortAscending) it else it.asReversed() }
                val connectedStatus =
                    networks
                        .firstOrNull { it.existInSystem && it.networkId == connectedNetworkId }
                        ?.let { WifiConnectionStatus.Connected(it.ssid, it.password) }
                ui.copy(
                    savedNetworks =
                        if (connectedStatus != null) {
                            val (connected, others) = sorted.partition(connectedStatus::matches)
                            connected + others
                        } else {
                            sorted
                        },
                    isCacheMode = mode == PrivilegedMode.NONE,
                    connectionStatus =
                        when {
                            connectedStatus != null -> connectedStatus
                            pendingNetwork != null ->
                                WifiConnectionStatus.Connecting(
                                    pendingNetwork.ssid,
                                    pendingNetwork.password,
                                )
                            else -> WifiConnectionStatus.Disconnected
                        },
                )
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5.seconds),
                // Cleared by the first combined emission, which waits for the initial refresh.
                initialValue =
                    State(isCacheMode = !privilegedManager.currentMode.hasPrivilegedAccess),
            )
    val searchState = TextFieldState()

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    @Volatile private var pendingImportFiles: List<PlatformFile> = emptyList()
    @Volatile private var pendingImportToDevice = false

    fun onAction(action: Action) {
        when (action) {
            is Action.Refresh -> onRefresh()
            is Action.ToggleSearch -> {
                searchState.clearText()
                _uiState.update { it.copy(showingSearch = !it.showingSearch) }
            }

            is Action.FilterToggled -> onFilterToggled(action.filter)
            is Action.SortChanged -> _uiState.update { it.copy(sortBy = action.sortBy) }
            is Action.SortDirectionChanged ->
                _uiState.update { it.copy(sortAscending = action.ascending) }
            is Action.ResetSortFilter -> onResetSortFilter()
            is Action.DeleteNote -> onDeleteNote(action.network)
            is Action.Disconnect -> onDisconnect()
            is Action.Connect -> onConnect(action.network)
            is Action.Forget -> onForget(action.network)
            is Action.ImportNetworks -> onImportNetworks()
            is Action.ChooseImportMode -> onChooseImportMode(action.toDevice)
            is Action.HideImportDialog -> onHideImportDialog()
            is Action.ConfirmImportWithPassword -> onConfirmImportWithPassword(action.password)
            is Action.ShowExportDialog -> onShowExportDialog()
            is Action.HideExportDialog -> _uiState.update { it.copy(showExportDialog = false) }
            is Action.ConfirmExport -> onExportNetworks(action.option, action.password)
            is Action.ShowForgetAllDialog -> onShowForgetAllDialog()
            is Action.HideForgetAllDialog ->
                _uiState.update { it.copy(showForgetAllDialog = false) }
            is Action.ConfirmForgetAllNetworks -> onForgetAllNetworks(action.clearCache)
        }
    }

    private fun onRefresh() {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            try {
                if (!wifiRepository.refresh()) return@launch
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.refresh_success)))
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun onFilterToggled(filter: Filter) {
        _uiState.update {
            it.copy(
                filters =
                    if (filter in it.filters) {
                        it.filters - filter
                    } else {
                        it.filters + filter
                    },
            )
        }
    }

    private fun onResetSortFilter() {
        _uiState.update {
            val default = State()
            it.copy(
                filters = default.filters,
                sortBy = default.sortBy,
                sortAscending = default.sortAscending,
            )
        }
    }

    private fun onDeleteNote(network: WifiNetwork) {
        viewModelScope.launch {
            wifiRepository.updateNote(network.ssid, network.password, null)
            _event.send(Event.ShowMessage(UiText.StringResource(R.string.note_deleted)))
        }
    }

    private fun onDisconnect() {
        viewModelScope.launch {
            val status = state.value.connectionStatus
            if (status is WifiConnectionStatus.Connected) {
                if (!wifiRepository.disconnect()) return@launch
                _event.send(
                    Event.ShowMessage(
                        UiText.StringResource(R.string.disconnected_from_message, status.ssid),
                    ),
                )
            }
        }
    }

    private fun onConnect(network: WifiNetwork) {
        viewModelScope.launch {
            _pendingNetwork.update { network }
            _event.send(
                Event.ShowMessage(
                    UiText.StringResource(R.string.connecting_to_message, network.ssid),
                ),
            )

            val connected = wifiRepository.connect(network)

            _pendingNetwork.update { null }
            _event.send(
                Event.ShowMessage(
                    UiText.StringResource(
                        if (connected) R.string.connected_to_message
                        else R.string.connect_failed_message,
                        network.ssid,
                    ),
                ),
            )
        }
    }

    private fun onForget(network: WifiNetwork) {
        viewModelScope.launch {
            wifiRepository.forgetNetwork(network)
            _event.send(
                Event.ShowMessage(UiText.StringResource(R.string.forgot_message, network.ssid)),
            )
        }
    }

    private fun onShowExportDialog() {
        viewModelScope.launch {
            val count = wifiRepository.getNetworkCount()
            if (count == 0) {
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.no_network_to_export)))
                return@launch
            }
            _uiState.update { it.copy(showExportDialog = true) }
        }
    }

    private fun onExportNetworks(option: ExportOption, password: String) {
        _uiState.update { it.copy(showExportDialog = false) }
        viewModelScope.launch {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
            val file =
                FileKit.openFileSaver(
                    suggestedName = "WiFi_${LocalDateTime.now().format(formatter)}",
                    defaultExtension = option.getFileExtension(password),
                ) ?: return@launch

            runCatching {
                    withContext(Dispatchers.IO) {
                        val networks = wifiRepository.getAllNetworksList().groupBySsidAndPassword()
                        file.write(fileRepository.encodeNetworks(networks, option, password))
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
        if (privilegedManager.mode.value.hasPrivilegedAccess) {
            _uiState.update { it.copy(showImportModeDialog = true) }
        } else {
            pickAndImport(toDevice = false)
        }
    }

    private fun onChooseImportMode(toDevice: Boolean) {
        _uiState.update { it.copy(showImportModeDialog = false) }
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

            val hasEncryptedFiles = files.any { it.extension.lowercase() == "bin" }
            if (hasEncryptedFiles) {
                pendingImportFiles = files
                pendingImportToDevice = toDevice
                _uiState.update { it.copy(showImportPasswordDialog = true) }
                return@launch
            }

            performImport(files, password = null, toDevice = toDevice)
        }
    }

    private fun onHideImportDialog() {
        pendingImportFiles = emptyList()
        _uiState.update { it.copy(showImportModeDialog = false, showImportPasswordDialog = false) }
    }

    private fun onConfirmImportWithPassword(password: String) {
        val files = pendingImportFiles
        pendingImportFiles = emptyList()
        _uiState.update { it.copy(showImportPasswordDialog = false) }
        performImport(files, password, pendingImportToDevice)
    }

    private fun performImport(files: List<PlatformFile>, password: String?, toDevice: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val result =
                    withContext(Dispatchers.IO) {
                        val networks = fileRepository.readNetworks(files, password)
                        wifiRepository.importNetworks(networks, toDevice)
                    }
                _event.send(Event.ShowMessage(UiText.StringResource(result.messageResId)))
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
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun onShowForgetAllDialog() {
        if (!privilegedManager.mode.value.hasPrivilegedAccess) return
        viewModelScope.launch {
            val count = wifiRepository.getNetworkCount()
            if (count == 0) {
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.no_network_to_forget)))
                return@launch
            }

            _uiState.update { it.copy(showForgetAllDialog = true) }
        }
    }

    private fun onForgetAllNetworks(clearCache: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showForgetAllDialog = false) }

            runCatching {
                    if (!wifiRepository.forgetAll(clearCache)) {
                        _uiState.update { it.copy(isLoading = false) }
                        return@launch
                    }
                }
                .fold(
                    onSuccess = {
                        _event.send(
                            Event.ShowMessage(UiText.StringResource(R.string.forget_success)),
                        )
                    },
                    onFailure = {
                        if (it is CancellationException) throw it
                        Log.e(TAG, "Failed to remove networks", it)
                        _event.send(
                            Event.ShowMessage(UiText.StringResource(R.string.forget_failed)),
                        )
                    },
                )

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
