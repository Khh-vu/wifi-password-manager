package io.github.wifi_password_manager.ui.screen.network.list

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.PrivilegedMode
import io.github.wifi_password_manager.domain.model.WifiConnectionStatus
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.manager.PrivilegedManager
import io.github.wifi_password_manager.utils.UiText
import io.github.wifi_password_manager.utils.groupAndSortedBySsid
import io.github.wifi_password_manager.utils.toWifiConfigurations
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class NetworkListViewModel(
    private val wifiRepository: WifiRepository,
    privilegedManager: PrivilegedManager,
) : ViewModel() {
    companion object {
        private const val TAG = "NetworkListViewModel"
    }

    enum class Filter {
        ALL, ON_DEVICE, APP_ONLY;

        val labelResId: Int
            @StringRes get() = when (this) {
                ALL -> R.string.filter_all
                ON_DEVICE -> R.string.filter_on_device
                APP_ONLY -> R.string.filter_app_only
            }
    }

    @Immutable
    data class State(
        val savedNetworks: List<WifiNetwork> = emptyList(),
        val showingSearch: Boolean = false,
        val connectionStatus: WifiConnectionStatus = WifiConnectionStatus.Disconnected,
        val searchText: String = "",
        val isCacheMode: Boolean = false,
        val showMethodSignatureError: Boolean = false,
        val filter: Filter = Filter.ALL,
    )

    sealed interface Action {
        data object Refresh : Action

        data object ToggleSearch : Action

        data class FilterChanged(val filter: Filter) : Action

        data class SearchTextChanged(val text: String) : Action

        data class DeleteNote(val ssid: String) : Action

        data object DismissMethodInspectorError : Action

        data object Disconnect : Action

        data class Connect(val network: WifiNetwork) : Action

        data class Forget(val network: WifiNetwork) : Action
    }

    sealed interface Event {
        data class ShowMessage(val message: UiText) : Event
    }

    private val _pendingNetwork = MutableStateFlow<WifiNetwork?>(null)
    private val _showingSearch = MutableStateFlow(false)
    private val _searchText = MutableStateFlow("")
    private val _showMethodSignatureError = MutableStateFlow(false)
    private val _filter = MutableStateFlow(Filter.ALL)
    private val _networks =
        _searchText.debounce(200.milliseconds).distinctUntilChanged().flatMapLatest { searchText ->
            val query = searchText.replace("[^\\p{L}\\p{N}\\s]".toRegex(), "").trim()
            if (query.isBlank()) {
                wifiRepository.getAllNetworks()
            } else {
                wifiRepository.getAllNetworks(query.lowercase())
            }
        }

    val state = combine(
        _networks,
        _searchText,
        _showingSearch,
        privilegedManager.mode.map { it == PrivilegedMode.NONE },
        wifiRepository.getConnectedWifiSsidFlow(),
        _showMethodSignatureError,
        _pendingNetwork,
        _filter,
    ) { args ->
        @Suppress("UNCHECKED_CAST") val networks = args[0] as List<WifiNetwork>
        val connectedSsid = args[4] as String
        val sortedNetworks = networks.groupAndSortedBySsid().let { grouped ->
            if (connectedSsid.isNotBlank()) {
                grouped.sortedByDescending { it.ssid == connectedSsid }
            } else {
                grouped
            }
        }
        val pendingNetwork = args[6] as WifiNetwork?
        val filter = args[7] as Filter

        State(
            savedNetworks = sortedNetworks.filter {
                when (filter) {
                    Filter.ALL -> true
                    Filter.ON_DEVICE -> it.existInSystem
                    Filter.APP_ONLY -> !it.existInSystem
                }
            },
            filter = filter,
            searchText = args[1] as String,
            showingSearch = args[2] as Boolean,
            isCacheMode = args[3] as Boolean,
            connectionStatus = if (connectedSsid.isNotBlank()) {
                WifiConnectionStatus.Connected(connectedSsid)
            } else if (pendingNetwork != null) {
                WifiConnectionStatus.Connecting(pendingNetwork.ssid)
            } else {
                WifiConnectionStatus.Disconnected
            },
            showMethodSignatureError = args[5] as Boolean,
        )
    }.onStart { refresh() }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = State(),
    )

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    fun onAction(action: Action) {
        Log.d(TAG, "onAction: $action")
        when (action) {
            is Action.Refresh -> onRefresh()
            is Action.ToggleSearch -> onToggleSearch()
            is Action.FilterChanged -> _filter.update { action.filter }
            is Action.SearchTextChanged -> _searchText.update { action.text }
            is Action.DeleteNote -> onDeleteNote(action.ssid)
            is Action.DismissMethodInspectorError -> _showMethodSignatureError.update { false }
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

    private suspend fun refresh(): Boolean {
        return try {
            wifiRepository.refresh()
            true
        } catch (e: NoSuchMethodException) {
            Log.e(TAG, "Method signature not found", e)
            _showMethodSignatureError.update { true }
            false
        }
    }

    private fun onToggleSearch() {
        _showingSearch.update { !it }
        _searchText.update { "" }
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

            val connected = withTimeoutOrNull(10.seconds) {
                wifiRepository.connect(network)
                wifiRepository.getConnectedWifiSsidFlow().first { it == network.ssid }
            } != null

            _pendingNetwork.update { null }
            _event.send(
                Event.ShowMessage(
                    UiText.StringResource(
                        if (connected) R.string.connected_to_message else R.string.connect_failed_message,
                        network.ssid,
                    ),
                ),
            )
        }
    }

    private fun onForget(network: WifiNetwork) {
        viewModelScope.launch {
            network.toWifiConfigurations().map { it.networkId }.filter { it != -1 }.toSet()
                .forEach { wifiRepository.forget(it) }
            wifiRepository.delete(network.ssid)
            refresh()
            _event.send(
                Event.ShowMessage(UiText.StringResource(R.string.forgot_message, network.ssid)),
            )
        }
    }
}
