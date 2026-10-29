package io.github.wifi_password_manager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val LocalNavBackStack = compositionLocalOf { NavBackStack<NavKey>() }

@Composable
fun rememberAppBackStack(): NavBackStack<NavKey> =
    rememberNavBackStack(
        configuration =
            SavedStateConfiguration {
                serializersModule = SerializersModule {
                    polymorphic(NavKey::class) {
                        subclass(Route.NetworkListScreen::class)
                        subclass(Route.SettingScreen::class)
                        subclass(Route.LockTimeoutScreen::class)
                        subclass(Route.LicenseScreen::class)
                        subclass(Route.NoteScreen::class)
                        subclass(Route.ExportWifiSetupScreen::class)
                        subclass(Route.IWifiManagerMethodInspectorScreen::class)
                    }
                }
            },
        Route.NetworkListScreen,
    )
