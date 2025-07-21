package io.github.wifi_password_manager.ui.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wifi_password_manager.data.WifiNetwork
import io.github.wifi_password_manager.services.WifiService
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(private val wifiService: WifiService) : ViewModel() {
    data class State(
        val savedNetworks: List<WifiNetwork> = emptyList(),
        val isLoading: Boolean = false,
        val showingSearch: Boolean = false,
        val searchText: String = "",
    )

    sealed interface Event {
        data object GetSavedNetworks : Event

        data object ToggleSearch : Event

        data class SearchTextChanged(val text: String) : Event
    }

    private val cachedNetworks = mutableListOf<WifiNetwork>()

    private val _state = MutableStateFlow(State())
    val state =
        _state
            .onStart { onEvent(Event.GetSavedNetworks) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5.seconds),
                initialValue = State(),
            )

    init {
        state
            .distinctUntilChangedBy { it.searchText }
            .map { state ->
                cachedNetworks.filter {
                    it.ssid.contains(state.searchText.trim(), ignoreCase = true)
                }
            }
            .onEach { networks -> _state.update { it.copy(savedNetworks = networks) } }
            .launchIn(viewModelScope)

        state
            .distinctUntilChangedBy { it.showingSearch }
            .map { it.showingSearch }
            .onEach { showing -> if (!showing) _state.update { it.copy(searchText = "") } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.GetSavedNetworks -> getSavedNetworks()
                is Event.ToggleSearch ->
                    _state.update { it.copy(showingSearch = !it.showingSearch) }
                is Event.SearchTextChanged -> _state.update { it.copy(searchText = event.text) }
            }
        }
    }

    private fun getSavedNetworks() {
        _state.update { it.copy(isLoading = true) }
        val networks = wifiService.getPrivilegedConfiguredNetworks()
        cachedNetworks += networks
        _state.update { it.copy(savedNetworks = networks, isLoading = false) }
    }
}
