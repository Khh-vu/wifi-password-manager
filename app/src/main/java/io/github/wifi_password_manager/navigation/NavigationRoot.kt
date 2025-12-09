package io.github.wifi_password_manager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.wifi_password_manager.ui.screen.license.LicenseView
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListView
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.screen.setting.SettingView
import io.github.wifi_password_manager.ui.screen.setting.SettingViewModel
import io.github.wifi_password_manager.utils.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable data object NetworkListScreen : NavKey

@Serializable data object SettingScreen : NavKey

@Serializable data object LicenseScreen : NavKey

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    CompositionLocalProvider(LocalNavBackStack provides rememberNavBackStack(NetworkListScreen)) {
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
                    entry<NetworkListScreen> {
                        val viewModel = koinViewModel<NetworkListViewModel>()
                        val state by viewModel.state.collectAsStateWithLifecycle()

                        LaunchedEffect(Unit) {
                            viewModel.event.collectLatest { event ->
                                when (event) {
                                    is NetworkListViewModel.Event.ShowMessage -> {
                                        context.toast(event.message.asString(context))
                                    }
                                }
                            }
                        }

                        NetworkListView(state = state, onAction = viewModel::onAction)
                    }

                    entry<SettingScreen> {
                        val viewModel = koinViewModel<SettingViewModel>()
                        val state by viewModel.state.collectAsStateWithLifecycle()

                        LaunchedEffect(Unit) {
                            viewModel.event.collectLatest { event ->
                                when (event) {
                                    is SettingViewModel.Event.ShowMessage -> {
                                        context.toast(event.message.asString(context))
                                    }
                                }
                            }
                        }

                        SettingView(state = state, onAction = viewModel::onAction)
                    }

                    entry<LicenseScreen> { LicenseView() }
                },
        )
    }
}

val LocalNavBackStack = compositionLocalOf { NavBackStack<NavKey>() }
