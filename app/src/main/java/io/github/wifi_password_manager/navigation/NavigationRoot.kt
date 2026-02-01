package io.github.wifi_password_manager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.wifi_password_manager.ui.screen.license.LicenseView
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListView
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.screen.note.NoteView
import io.github.wifi_password_manager.ui.screen.note.NoteViewModel
import io.github.wifi_password_manager.ui.screen.setting.SettingView
import io.github.wifi_password_manager.ui.screen.setting.SettingViewModel
import io.github.wifi_password_manager.ui.shared.ObserveAsEvent
import io.github.wifi_password_manager.utils.toast
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val context = LocalContext.current
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
                    entry<Route.NetworkListScreen> {
                        val viewModel = koinViewModel<NetworkListViewModel>()
                        val state by viewModel.state.collectAsStateWithLifecycle()

                        ObserveAsEvent(viewModel.event) { event ->
                            when (event) {
                                is NetworkListViewModel.Event.ShowMessage -> {
                                    context.toast(event.message.asString(context))
                                }
                            }
                        }

                        NetworkListView(state = state, onAction = viewModel::onAction)
                    }

                    entry<Route.SettingScreen> {
                        val viewModel = koinViewModel<SettingViewModel>()
                        val state by viewModel.state.collectAsStateWithLifecycle()

                        ObserveAsEvent(viewModel.event) { event ->
                            when (event) {
                                is SettingViewModel.Event.ShowMessage -> {
                                    context.toast(event.message.asString(context))
                                }
                            }
                        }

                        SettingView(state = state, onAction = viewModel::onAction)
                    }

                    entry<Route.LicenseScreen> { LicenseView() }

                    entry<Route.NoteScreen> {
                        val viewModel = koinViewModel<NoteViewModel> { parametersOf(it.network) }
                        val state by viewModel.state.collectAsStateWithLifecycle()

                        ObserveAsEvent(viewModel.event) { event ->
                            when (event) {
                                is NoteViewModel.Event.ShowMessage -> {
                                    context.toast(event.message.asString(context))
                                }
                                is NoteViewModel.Event.NavigateBack -> {
                                    backStack.removeLastOrNull()
                                }
                            }
                        }

                        NoteView(
                            network = it.network,
                            state = state,
                            onAction = viewModel::onAction,
                        )
                    }
                },
        )
    }
}

val LocalNavBackStack = compositionLocalOf { NavBackStack<NavKey>() }
