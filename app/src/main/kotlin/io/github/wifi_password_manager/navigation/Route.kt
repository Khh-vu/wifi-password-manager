package io.github.wifi_password_manager.navigation

import androidx.navigation3.runtime.NavKey
import io.github.wifi_password_manager.domain.model.WifiNetwork
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable data object NetworkListScreen : Route, NavKey

    @Serializable data object SettingScreen : Route, NavKey

    @Serializable data object LockTimeoutScreen : Route, NavKey

    @Serializable data object LicenseScreen : Route, NavKey

    @Serializable data class NoteScreen(val network: WifiNetwork) : Route, NavKey

    @Serializable data object ExportWifiSetupScreen : Route, NavKey

    @Serializable data object IWifiManagerMethodInspectorScreen : Route, NavKey
}
