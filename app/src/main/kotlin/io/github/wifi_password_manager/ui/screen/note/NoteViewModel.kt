package io.github.wifi_password_manager.ui.screen.note

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.utils.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteViewModel(private val wifiRepository: WifiRepository, private val network: WifiNetwork) :
    ViewModel() {
    companion object {
        private const val TAG = "NoteViewModel"
    }

    sealed interface Action {
        data class UpdateNote(val note: String) : Action

        data object SaveOrDeleteNote : Action
    }

    sealed interface Event {
        data class ShowMessage(val message: UiText) : Event

        data object NavigateBack : Event
    }

    val state: StateFlow<String>
        field = MutableStateFlow(network.note.orEmpty())

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    fun onAction(action: Action) {
        Log.d(TAG, "onAction: $action")
        when (action) {
            is Action.UpdateNote -> state.update { action.note }
            is Action.SaveOrDeleteNote -> onSaveOrDeleteNote()
        }
    }

    private fun onSaveOrDeleteNote() {
        viewModelScope.launch {
            if (network.note == state.value) {
                _event.send(Event.NavigateBack)
                return@launch
            }

            val note = state.value.trim().ifBlank { null }
            runCatching { wifiRepository.updateNote(network.ssid, note) }
                .fold(
                    onSuccess = {
                        wifiRepository.refresh()
                        _event.send(
                            Event.ShowMessage(
                                UiText.StringResource(
                                    if (note == null) R.string.empty_note_discarded
                                    else R.string.note_saved
                                )
                            )
                        )
                        _event.send(Event.NavigateBack)
                    },
                    onFailure = {
                        Log.e(TAG, "Failed to update note", it)
                        _event.send(
                            Event.ShowMessage(UiText.StringResource(R.string.note_save_failed))
                        )
                    },
                )
        }
    }
}
