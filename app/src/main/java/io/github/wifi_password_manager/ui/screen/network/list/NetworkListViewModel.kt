package io.github.wifi_password_manager.ui.screen.network.list

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.utils.UiText
import io.github.wifi_password_manager.utils.groupAndSortedBySsid
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class NetworkListViewModel(private val wifiRepository: WifiRepository) : ViewModel() {
    companion object {
        private const val TAG = "NetworkListViewModel"
    }

@Immutable
    data class State(
        val savedNetworks: List<WifiNetwork> = emptyList(),
        val showingSearch: Boolean = false,
        val searchText: String = "",
    )

    sealed interface Action {
        data object Refresh : Action

        data object ToggleSearch : Action

        data class SearchTextChanged(val text: String) : Action

        data class DeleteNote(val ssid: String) : Action
    }

    sealed interface Event {
        data class ShowMessage(val message: UiText) : Event
    }

    private val _state = MutableStateFlow(State())
    val state =
        _state
            .onStart { wifiRepository.refresh() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5.seconds),
                initialValue = State(),
            )

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    init {
        state
            .map { it.searchText }
            .debounce(200.milliseconds)
            .distinctUntilChanged()
            .flatMapLatest { searchText ->
                val query = searchText.replace("[^a-zA-Z0-9\\\\s]".toRegex(), "").trim()
                if (query.isBlank()) {
                    wifiRepository.getAllNetworks()
                } else {
                    wifiRepository.getAllNetworks("*$query*")
                }
            }
            .map { it.groupAndSortedBySsid() }
            .onEach { networks -> _state.update { it.copy(savedNetworks = networks) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: Action) {
        Log.d(TAG, "onAction: $action")
        when (action) {
            is Action.Refresh -> onRefresh()
            is Action.ToggleSearch -> onToggleSearch()
            is Action.SearchTextChanged -> _state.update { it.copy(searchText = action.text) }
            is Action.DeleteNote -> onDeleteNote(action.ssid)
        }
    }

    private fun onRefresh() {
        viewModelScope.launch {
            wifiRepository.refresh()
            _event.send(Event.ShowMessage(UiText.StringResource(R.string.refresh_success)))
        }
    }

    private fun onToggleSearch() {
        _state.update { it.copy(showingSearch = !it.showingSearch, searchText = "") }
    }

    private fun onDeleteNote(ssid: String) {
        viewModelScope.launch {
            wifiRepository.updateNote(ssid, null)
            _event.send(Event.ShowMessage(UiText.StringResource(R.string.note_deleted)))
        }
    }
}
