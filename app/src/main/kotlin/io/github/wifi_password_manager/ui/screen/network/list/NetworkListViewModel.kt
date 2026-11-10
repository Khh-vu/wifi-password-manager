package io.github.wifi_password_manager.ui.screen.network.list

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.PrivilegedMode
import io.github.wifi_password_manager.domain.model.WifiConnectionStatus
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.model.WifiNetwork.SecurityType
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.manager.PrivilegedManager
import io.github.wifi_password_manager.utils.UiText
import io.github.wifi_password_manager.utils.groupAndSortedBySsid
import io.github.wifi_password_manager.utils.toWifiConfigurations
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class NetworkListViewModel(
    private val wifiRepository: WifiRepository,
    privilegedManager: PrivilegedManager,
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
        val showMethodSignatureError: Boolean = false,
        val filters: Set<Filter> = emptySet(),
        val sortBy: SortBy = SortBy.SSID,
        val sortAscending: Boolean = true,
    )

    sealed interface Action {
        data object Refresh : Action

        data object ToggleSearch : Action

        data class FilterToggled(val filter: Filter) : Action

        data class SortChanged(val sortBy: SortBy) : Action

        data class SortDirectionChanged(val ascending: Boolean) : Action

        data object ResetSortFilter : Action

        data class DeleteNote(val ssid: String) : Action

        data object DismissMethodInspectorError : Action

        data object Disconnect : Action

        data class Connect(val network: WifiNetwork) : Action

        data class Forget(val network: WifiNetwork) : Action
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
                wifiRepository.getConnectedWifiSsidFlow().onEach { refresh() },
                _pendingNetwork,
            ) { ui, networks, mode, connectedSsid, pendingNetwork ->
                val sorted =
                    networks
                        .groupAndSortedBySsid()
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
                ui.copy(
                    savedNetworks =
                        if (connectedSsid.isNotBlank()) {
                            val (connected, others) = sorted.partition { it.ssid == connectedSsid }
                            connected + others
                        } else {
                            sorted
                        },
                    isCacheMode = mode == PrivilegedMode.NONE,
                    connectionStatus =
                        when {
                            connectedSsid.isNotBlank() ->
                                WifiConnectionStatus.Connected(connectedSsid)
                            pendingNetwork != null ->
                                WifiConnectionStatus.Connecting(pendingNetwork.ssid)
                            else -> WifiConnectionStatus.Disconnected
                        },
                )
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5.seconds),
                initialValue = State(),
            )
    val searchState = TextFieldState()

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

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
            is Action.DeleteNote -> onDeleteNote(action.ssid)
            is Action.DismissMethodInspectorError ->
                _uiState.update { it.copy(showMethodSignatureError = false) }

            is Action.Disconnect -> onDisconnect()
            is Action.Connect -> onConnect(action.network)
            is Action.Forget -> onForget(action.network)
        }
    }

    private fun onRefresh() {
        viewModelScope.launch {
            if (!refresh()) return@launch
            _event.send(Event.ShowMessage(UiText.StringResource(R.string.refresh_success)))
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

    private suspend fun refresh(): Boolean {
        return try {
            wifiRepository.refresh()
            true
        } catch (e: NoSuchMethodException) {
            Log.e(TAG, "Method signature not found", e)
            _uiState.update { it.copy(showMethodSignatureError = true) }
            false
        }
    }

    private fun onDeleteNote(ssid: String) {
        viewModelScope.launch {
            wifiRepository.updateNote(ssid, null)
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

            val connected =
                withTimeoutOrNull(10.seconds) {
                    wifiRepository.connect(network)
                    wifiRepository.getConnectedWifiSsidFlow().first { it == network.ssid }
                } != null

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
            network
                .toWifiConfigurations()
                .map { it.networkId }
                .filter { it != -1 }
                .toSet()
                .forEach { wifiRepository.forget(it) }
            wifiRepository.delete(network.ssid)
            refresh()
            _event.send(
                Event.ShowMessage(UiText.StringResource(R.string.forgot_message, network.ssid)),
            )
        }
    }
}
