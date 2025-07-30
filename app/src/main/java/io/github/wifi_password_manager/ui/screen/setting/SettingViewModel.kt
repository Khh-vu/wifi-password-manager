package io.github.wifi_password_manager.ui.screen.setting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wifi_password_manager.data.Settings
import io.github.wifi_password_manager.services.SettingService
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingViewModel(private val settingService: SettingService) : ViewModel() {
    companion object {
        private const val TAG = "SettingViewModel"
    }

    sealed interface Event {
        data class UpdateThemeMode(val themeMode: Settings.ThemeMode) : Event

        data class ToggleMaterialYou(val value: Boolean) : Event
    }

    val state =
        settingService.settings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = Settings(),
        )

    fun onEvent(event: Event) {
        Log.d(TAG, "onEvent: $event")
        viewModelScope.launch {
            when (event) {
                is Event.UpdateThemeMode ->
                    settingService.updateSettings { it.copy(themeMode = event.themeMode) }
                is Event.ToggleMaterialYou ->
                    settingService.updateSettings { it.copy(useMaterialYou = event.value) }
            }
        }
    }
}
