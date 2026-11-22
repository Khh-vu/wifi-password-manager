package io.github.wifi_password_manager.ui.screen.network.add

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.manager.PrivilegedManager
import io.github.wifi_password_manager.utils.UiText
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddNetworkViewModel(
    private val wifiRepository: WifiRepository,
    privilegedManager: PrivilegedManager,
) : ViewModel() {
    companion object {
        private const val TAG = "AddNetworkViewModel"

        @StringRes
        fun ssidError(ssid: String): Int? =
            R.string.ssid_error.takeIf { ssid.isEmpty() || ssid.encodeToByteArray().size > 32 }

        @StringRes
        fun passwordError(security: WifiNetwork.SecurityType, password: String): Int? =
            when (security) {
                WifiNetwork.SecurityType.OPEN,
                WifiNetwork.SecurityType.OWE -> null
                WifiNetwork.SecurityType.WPA2 ->
                    R.string.password_error_wpa2.takeIf { password.length !in 8..63 }
                WifiNetwork.SecurityType.WPA3 ->
                    R.string.password_error_wpa3.takeIf { password.length !in 1..63 }
                WifiNetwork.SecurityType.WEP ->
                    R.string.password_error_empty.takeIf { password.isEmpty() }
            }
    }

    @Immutable
    data class State(
        val security: WifiNetwork.SecurityType = WifiNetwork.SecurityType.entries.first(),
        val hidden: Boolean = false,
        val meteredOverride: WifiNetwork.MeteredOverride = WifiNetwork.MeteredOverride.NONE,
        val macRandomization: WifiNetwork.Randomization = WifiNetwork.Randomization.default,
        val saveToDevice: Boolean = false,
        val canSaveToDevice: Boolean = false,
        val isSaving: Boolean = false,
    )

    sealed interface Action {
        data class SecurityChanged(val security: WifiNetwork.SecurityType) : Action

        data class HiddenToggled(val hidden: Boolean) : Action

        data class MeteredChanged(val meteredOverride: WifiNetwork.MeteredOverride) : Action

        data class MacRandomizationChanged(val macRandomization: WifiNetwork.Randomization) : Action

        data class SaveToDeviceToggled(val saveToDevice: Boolean) : Action

        data object Save : Action
    }

    sealed interface Event {
        data class ShowMessage(val message: UiText) : Event

        data object NavigateBack : Event
    }

    val ssidState = TextFieldState()
    val passwordState = TextFieldState()
    val noteState = TextFieldState()

    private val _uiState = MutableStateFlow(State())

    val state =
        combine(_uiState, privilegedManager.mode) { ui, mode ->
                ui.copy(canSaveToDevice = mode.hasPrivilegedAccess)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5.seconds),
                initialValue = State(),
            )

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    fun onAction(action: Action) {
        when (action) {
            is Action.SecurityChanged -> _uiState.update { it.copy(security = action.security) }
            is Action.HiddenToggled -> _uiState.update { it.copy(hidden = action.hidden) }
            is Action.MeteredChanged ->
                _uiState.update { it.copy(meteredOverride = action.meteredOverride) }
            is Action.MacRandomizationChanged ->
                _uiState.update { it.copy(macRandomization = action.macRandomization) }
            is Action.SaveToDeviceToggled ->
                _uiState.update { it.copy(saveToDevice = action.saveToDevice) }
            is Action.Save -> onSave()
        }
    }

    private fun onSave() {
        val current = state.value
        if (current.isSaving) return
        val ssid = ssidState.text.toString()
        val password =
            if (
                current.security !in
                    setOf(
                        WifiNetwork.SecurityType.OPEN,
                        WifiNetwork.SecurityType.OWE,
                    )
            ) {
                passwordState.text.toString()
            } else {
                ""
            }
        if (ssidError(ssid) != null || passwordError(current.security, password) != null) return

        val network =
            WifiNetwork(
                ssid = ssid,
                securityType = setOf(current.security),
                password = password,
                hidden = current.hidden,
                meteredOverride = current.meteredOverride.value,
                macRandomizationSetting = current.macRandomization.value,
                note = noteState.text.toString().trim().ifEmpty { null },
                existInSystem = false,
            )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                    when {
                        !wifiRepository.addManual(network) -> R.string.network_already_saved
                        current.saveToDevice &&
                            current.canSaveToDevice &&
                            !wifiRepository.saveToSystem(listOf(network)) ->
                            R.string.network_saved_app_only
                        else -> R.string.network_added
                    }
                }
                .fold(
                    onSuccess = { message ->
                        _event.send(Event.ShowMessage(UiText.StringResource(message)))
                        if (message != R.string.network_already_saved) {
                            _event.send(Event.NavigateBack)
                        }
                    },
                    onFailure = {
                        Log.e(TAG, "Failed to add network", it)
                        _event.send(
                            Event.ShowMessage(UiText.StringResource(R.string.network_add_failed)),
                        )
                    },
                )
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
