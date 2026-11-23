package io.github.wifi_password_manager.ui.screen.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.wifi_password_manager.domain.model.Settings
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.manager.PrivilegedManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingViewModel(
    private val settingRepository: SettingRepository,
    privilegedManager: PrivilegedManager,
) : ViewModel() {
    sealed interface Action {
        data class UpdateLanguage(val language: Settings.Language) : Action

        data class UpdateThemeMode(val themeMode: Settings.ThemeMode) : Action

        data class ToggleMaterialYou(val value: Boolean) : Action

        data class TogglePureBlackTheme(val value: Boolean) : Action

        data class ToggleSearchBarAtBottom(val value: Boolean) : Action

        data class ToggleAppLock(val value: Boolean) : Action

        data class ToggleSecureScreen(val value: Boolean) : Action

        data class TogglePlaintextPasswords(val value: Boolean) : Action

        data class ToggleAutoPersistEphemeralNetworks(val value: Boolean) : Action

        data class ToggleAutoSyncNetworks(val value: Boolean) : Action

        data class ToggleAllowCacheMode(val value: Boolean) : Action

        data class ToggleAllowInsecureReceiver(val value: Boolean) : Action
    }

    sealed interface Event {
        data class UpdateThemeMode(val themeMode: Settings.ThemeMode) : Event
    }

    val mode = privilegedManager.mode

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    fun onAction(action: Action) {
        when (action) {
            is Action.UpdateLanguage -> updateSettings { it.copy(language = action.language) }
            is Action.UpdateThemeMode -> onUpdateThemeMode(action.themeMode)
            is Action.ToggleMaterialYou -> updateSettings { it.copy(useMaterialYou = action.value) }
            is Action.TogglePureBlackTheme ->
                updateSettings { it.copy(pureBlackTheme = action.value) }

            is Action.ToggleSearchBarAtBottom ->
                updateSettings { it.copy(searchBarAtBottom = action.value) }

            is Action.ToggleAppLock -> updateSettings { it.copy(appLockEnabled = action.value) }
            is Action.ToggleSecureScreen ->
                updateSettings {
                    it.copy(secureScreenEnabled = action.value, plaintextPasswords = false)
                }

            is Action.TogglePlaintextPasswords ->
                updateSettings { it.copy(plaintextPasswords = action.value) }

            is Action.ToggleAutoPersistEphemeralNetworks ->
                updateSettings { it.copy(autoPersistEphemeralNetworks = action.value) }

            is Action.ToggleAutoSyncNetworks ->
                updateSettings { it.copy(autoSyncNetworks = action.value) }

            is Action.ToggleAllowCacheMode ->
                updateSettings { it.copy(allowCacheMode = action.value) }

            is Action.ToggleAllowInsecureReceiver ->
                updateSettings { it.copy(allowInsecureReceiver = action.value) }
        }
    }

    private fun updateSettings(block: (Settings) -> Settings) {
        viewModelScope.launch { settingRepository.updateSettings(block) }
    }

    private fun onUpdateThemeMode(themeMode: Settings.ThemeMode) {
        updateSettings { it.copy(themeMode = themeMode) }
        viewModelScope.launch { _event.send(Event.UpdateThemeMode(themeMode)) }
    }
}
