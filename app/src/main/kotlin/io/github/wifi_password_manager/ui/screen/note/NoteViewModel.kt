package io.github.wifi_password_manager.ui.screen.note

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.utils.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class NoteViewModel(private val wifiRepository: WifiRepository, private val network: WifiNetwork) :
    ViewModel() {
    companion object {
        private const val TAG = "NoteViewModel"
    }

    sealed interface Action {
        data object SaveOrDeleteNote : Action
    }

    sealed interface Event {
        data class ShowMessage(val message: UiText) : Event

        data object NavigateBack : Event
    }

    val noteState: TextFieldState = TextFieldState(network.note.orEmpty())

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    fun onAction(action: Action) {
        when (action) {
            is Action.SaveOrDeleteNote -> onSaveOrDeleteNote()
        }
    }

    private fun onSaveOrDeleteNote() {
        viewModelScope.launch {
            if (network.note == noteState.text) {
                _event.send(Event.NavigateBack)
                return@launch
            }

            val note = noteState.text.toString().trim().ifBlank { null }
            runCatching { wifiRepository.updateNote(network.ssid, network.password, note) }
                .fold(
                    onSuccess = {
                        wifiRepository.refresh()
                        _event.send(
                            Event.ShowMessage(
                                UiText.StringResource(
                                    if (note == null) R.string.empty_note_discarded
                                    else R.string.note_saved,
                                ),
                            ),
                        )
                        _event.send(Event.NavigateBack)
                    },
                    onFailure = {
                        Log.e(TAG, "Failed to update note", it)
                        _event.send(
                            Event.ShowMessage(UiText.StringResource(R.string.note_save_failed)),
                        )
                    },
                )
        }
    }
}
