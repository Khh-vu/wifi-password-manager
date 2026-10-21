package io.github.wifi_password_manager.navigation

import android.net.wifi.IWifiManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.wifi_password_manager.ui.screen.integration.ExportWifiSetupView
import io.github.wifi_password_manager.ui.screen.license.LicenseView
import io.github.wifi_password_manager.ui.screen.methodinspector.MethodInspectorScreen
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListScreen
import io.github.wifi_password_manager.ui.screen.note.NoteScreen
import io.github.wifi_password_manager.ui.screen.setting.SettingScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack =
        rememberNavBackStack(
            configuration =
                SavedStateConfiguration {
                    serializersModule = SerializersModule {
                        polymorphic(NavKey::class) {
                            subclass(Route.NetworkListScreen::class)
                            subclass(Route.SettingScreen::class)
                            subclass(Route.LicenseScreen::class)
                            subclass(Route.NoteScreen::class)
                            subclass(Route.ExportWifiSetupScreen::class)
                            subclass(Route.IWifiManagerMethodInspectorScreen::class)
                        }
                    }
                },
            Route.NetworkListScreen,
        )

    CompositionLocalProvider(LocalNavBackStack provides backStack) {
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
                    entry<Route.LicenseScreen> { LicenseView() }
                    entry<Route.NoteScreen> { NoteScreen(it.network) }
                    entry<Route.ExportWifiSetupScreen> { ExportWifiSetupView() }
                    entry<Route.IWifiManagerMethodInspectorScreen> {
                        MethodInspectorScreen(IWifiManager::class)
                    }
                },
        )
    }
}

val LocalNavBackStack = compositionLocalOf { NavBackStack<NavKey>() }
