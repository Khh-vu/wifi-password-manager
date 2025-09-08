package io.github.wifi_password_manager.ui.screen.network.list

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.data.WifiNetwork
import io.github.wifi_password_manager.services.WifiService
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class NetworkListViewModel(private val wifiService: WifiService) : ViewModel() {
    companion object {
        private const val TAG = "NetworkListViewModel"
    }

    @Immutable
    data class State(
        val savedNetworks: ImmutableList<WifiNetwork> = persistentListOf(),
        val showingSearch: Boolean = false,
        val searchText: String = "",
    )

    sealed interface Action {
        data object Refresh : Action

        data object ToggleSearch : Action

        data class SearchTextChanged(val text: String) : Action
    }

    sealed interface Event {
        data class ShowMessage(val messageRes: Int) : Event
    }

    private val _state = MutableStateFlow(State())
    val state =
        _state
            .onStart { wifiService.refresh() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5.seconds),
                initialValue = State(),
            )

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    init {
        combine(
                wifiService.configuredNetworks,
                state.map { it.searchText }.debounce(200.milliseconds),
            ) { networks, searchText ->
                if (searchText.isBlank()) {
                    networks
                } else {
                    networks
                        .filter { it.ssid.contains(searchText.trim(), ignoreCase = true) }
                        .sortedBy { it.ssid.lowercase() }
                }
            }
            .onEach { networks ->
                _state.update { it.copy(savedNetworks = networks.toImmutableList()) }
            }
            .launchIn(viewModelScope)

        state
            .distinctUntilChangedBy { it.showingSearch }
            .map { it.showingSearch }
            .onEach { showing -> if (!showing) _state.update { it.copy(searchText = "") } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: Action) {
        Log.d(TAG, "onAction: $action")
        viewModelScope.launch {
            when (action) {
                is Action.Refresh -> onRefresh()
                is Action.ToggleSearch ->
                    _state.update { it.copy(showingSearch = !it.showingSearch) }

                is Action.SearchTextChanged -> _state.update { it.copy(searchText = action.text) }
            }
        }
    }

    private suspend fun onRefresh() {
        wifiService.refresh()
        _event.emit(Event.ShowMessage(R.string.refresh_success))
    }
}
