package io.github.wifi_password_manager.ui.screen.locktimeout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wifi_password_manager.domain.repository.SettingRepository
import kotlin.time.Duration
import kotlinx.coroutines.launch

class LockTimeoutViewModel(private val settingRepository: SettingRepository) : ViewModel() {
    sealed interface Action {
        data class UpdateLockTimeout(val lockTimeout: Duration) : Action
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.UpdateLockTimeout ->
                viewModelScope.launch {
                    settingRepository.updateSettings { it.copy(lockTimeout = action.lockTimeout) }
                }
        }
    }
}
