package io.github.wifi_password_manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.manager.PrivilegedManager
import io.github.wifi_password_manager.utils.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

class MainViewModel(
    private val settingRepository: SettingRepository,
    private val privilegedManager: PrivilegedManager,
    private val isBiometricSupported: Boolean,
) : ViewModel() {
    sealed interface Event {
        data class ShowMessage(val message: UiText) : Event
    }

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    private val _skipPrivilegedCheck = MutableStateFlow(false)
    val skipPrivilegedCheck = _skipPrivilegedCheck.asStateFlow()

    val privilegedMode = privilegedManager.mode

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    private val shizukuBinderReceivedListener = Shizuku.OnBinderReceivedListener {
        privilegedManager.refresh()
    }

    private val shizukuBinderDeadListener = Shizuku.OnBinderDeadListener {
        privilegedManager.refresh()
    }

    init {
        Shizuku.addBinderReceivedListenerSticky(shizukuBinderReceivedListener)
        Shizuku.addBinderDeadListener(shizukuBinderDeadListener)
        viewModelScope.launch {
            val settings = settingRepository.settings.value
            if (settings.appLockEnabled && !isBiometricSupported) {
                settingRepository.updateSettings { it.copy(appLockEnabled = false) }
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.app_lock_disabled)))
            }
            _isAuthenticated.update { !settingRepository.settings.value.appLockEnabled }
        }
    }

    override fun onCleared() {
        Shizuku.removeBinderReceivedListener(shizukuBinderReceivedListener)
        Shizuku.removeBinderDeadListener(shizukuBinderDeadListener)
        super.onCleared()
    }

    fun onAuthenticated() = _isAuthenticated.update { true }

    fun onSkipPrivilegedCheck() = _skipPrivilegedCheck.update { true }
}
