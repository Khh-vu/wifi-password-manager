package io.github.wifi_password_manager.navigation

import android.net.wifi.IWifiManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.wifi_password_manager.ui.screen.integration.ExportWifiSetupView
import io.github.wifi_password_manager.ui.screen.license.LicenseView
import io.github.wifi_password_manager.ui.screen.locktimeout.LockTimeoutScreen
import io.github.wifi_password_manager.ui.screen.methodinspector.MethodInspectorScreen
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListScreen
import io.github.wifi_password_manager.ui.screen.note.NoteScreen
import io.github.wifi_password_manager.ui.screen.setting.SettingScreen

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack = LocalNavBackStack.current

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                entry<Route.NetworkListScreen> { NetworkListScreen() }
                entry<Route.SettingScreen> { SettingScreen() }
                entry<Route.LockTimeoutScreen> { LockTimeoutScreen() }
                entry<Route.LicenseScreen> { LicenseView() }
                entry<Route.NoteScreen> { NoteScreen(it.network) }
                entry<Route.ExportWifiSetupScreen> { ExportWifiSetupView() }
                entry<Route.IWifiManagerMethodInspectorScreen> {
                    MethodInspectorScreen(IWifiManager::class)
                }
            },
    )
}
