package io.github.wifi_password_manager

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.manager.PrivilegedManager
import io.github.wifi_password_manager.utils.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    val isAuthenticated: StateFlow<Boolean>
        field = MutableStateFlow(false)
    val skipPrivilegedCheck: StateFlow<Boolean>
        field = MutableStateFlow(false)
    val privilegedMode = privilegedManager.mode

    private var job: Job? = null
    private var lockDeadline: Long? = null

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    private val shizukuBinderReceivedListener = Shizuku.OnBinderReceivedListener {
        viewModelScope.launch { privilegedManager.refresh() }
    }

    private val shizukuBinderDeadListener = Shizuku.OnBinderDeadListener {
        viewModelScope.launch { privilegedManager.refresh() }
    }

    init {
        Shizuku.addBinderReceivedListenerSticky(shizukuBinderReceivedListener)
        Shizuku.addBinderDeadListener(shizukuBinderDeadListener)
        viewModelScope.launch {
            val settings = settingRepository.getCurrentSettings()
            if (settings.appLockEnabled && !isBiometricSupported) {
                settingRepository.updateSettings { it.copy(appLockEnabled = false) }
                _event.send(Event.ShowMessage(UiText.StringResource(R.string.app_lock_disabled)))
            }
            isAuthenticated.update { !settingRepository.getCurrentSettings().appLockEnabled }
        }
    }

    override fun onCleared() {
        Shizuku.removeBinderReceivedListener(shizukuBinderReceivedListener)
        Shizuku.removeBinderDeadListener(shizukuBinderDeadListener)
    }

    fun onStopped() {
        job?.cancel()
        job = viewModelScope.launch {
            val settings = settingRepository.getCurrentSettings()
            if (!settings.appLockEnabled || settings.lockTimeout.isInfinite()) return@launch
            lockDeadline = SystemClock.elapsedRealtime() + settings.lockTimeout.inWholeMilliseconds
        }
    }

    fun onStarted() {
        job?.cancel()
        val deadline = lockDeadline
        if (deadline != null && SystemClock.elapsedRealtime() >= deadline) {
            isAuthenticated.update { false }
        }
        lockDeadline = null
    }

    fun onAuthenticated() = isAuthenticated.update { true }

    fun onSkipPrivilegedCheck() = skipPrivilegedCheck.update { true }

    suspend fun refreshPrivilegedMode() = privilegedManager.refresh()
}
