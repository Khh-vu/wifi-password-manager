package io.github.wifi_password_manager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import io.github.wifi_password_manager.ui.screen.main.MainView
import io.github.wifi_password_manager.ui.screen.main.MainViewModel
import io.github.wifi_password_manager.ui.screen.setting.SettingView
import io.github.wifi_password_manager.ui.screen.setting.SettingViewModel
import io.github.wifi_password_manager.utils.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable data object MainScreen : NavKey

@Serializable data object SettingScreen : NavKey

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    CompositionLocalProvider(LocalNavBackStack provides rememberNavBackStack(MainScreen)) {
        val backStack = LocalNavBackStack.current

        NavDisplay(
            modifier = modifier,
            backStack = backStack,
            onBack = { repeat(it) { if (backStack.size > 1) backStack.removeLastOrNull() } },
            entryDecorators =
                listOf(
                    rememberSavedStateNavEntryDecorator(),
                    rememberSceneSetupNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            entryProvider =
                entryProvider {
                    entry<MainScreen> {
                        val viewModel = koinViewModel<MainViewModel>()
                        val state by viewModel.state.collectAsStateWithLifecycle()

                        LaunchedEffect(Unit) {
                            viewModel.event.collectLatest { event ->
                                when (event) {
                                    is MainViewModel.Event.ShowMessage -> {
                                        context.toast(resId = event.messageRes)
                                    }
                                }
                            }
                        }

                        MainView(state = state, onAction = viewModel::onAction)
                    }

                    entry<SettingScreen> {
                        val viewModel = koinViewModel<SettingViewModel>()
                        val state by viewModel.state.collectAsStateWithLifecycle()

                        LaunchedEffect(Unit) {
                            viewModel.event.collectLatest { event ->
                                when (event) {
                                    is SettingViewModel.Event.ShowMessage -> {
                                        context.toast(resId = event.messageRes)
                                    }
                                }
                            }
                        }

                        SettingView(state = state, onAction = viewModel::onAction)
                    }
                },
        )
    }
}

val LocalNavBackStack = compositionLocalOf<NavBackStack> { mutableStateListOf() }
