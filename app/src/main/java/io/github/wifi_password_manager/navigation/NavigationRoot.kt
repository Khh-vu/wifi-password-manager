package io.github.wifi_password_manager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.wifi_password_manager.ui.screen.main.MainView
import io.github.wifi_password_manager.ui.screen.main.MainViewModel
import io.github.wifi_password_manager.ui.screen.setting.SettingView
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable data object MainScreen : NavKey

@Serializable data object SettingScreen : NavKey

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(MainScreen)

    CompositionLocalProvider(LocalNavBackStack provides backStack) {
        NavDisplay(modifier = modifier, backStack = LocalNavBackStack.current) { key ->
            when (key) {
                is MainScreen -> {
                    NavEntry(key = key) {
                        val viewModel = koinViewModel<MainViewModel>()
                        val state by viewModel.state.collectAsStateWithLifecycle()

                        MainView(state = state, onEvent = viewModel::onEvent)
                    }
                }

                is SettingScreen -> {
                    NavEntry(key = key) { SettingView() }
                }

                else -> throw RuntimeException("Invalid NavKey.")
            }
        }
    }
}

val LocalNavBackStack = compositionLocalOf<NavBackStack> { mutableStateListOf() }
