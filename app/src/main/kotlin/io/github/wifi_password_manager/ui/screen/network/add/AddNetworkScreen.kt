package io.github.wifi_password_manager.ui.screen.network.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.ui.shared.ObserveAsEvent
import io.github.wifi_password_manager.utils.toast
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddNetworkScreen() {
    val context = LocalContext.current
    val resources = LocalResources.current
    val navBackStack = LocalNavBackStack.current

    val viewModel = koinViewModel<AddNetworkViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvent(viewModel.event) { event ->
        when (event) {
            is AddNetworkViewModel.Event.ShowMessage -> {
                context.toast(event.message.asString(resources))
            }

            is AddNetworkViewModel.Event.NavigateBack -> {
                navBackStack.removeLastOrNull()
            }
        }
    }

    AddNetworkView(
        state = state,
        ssidState = viewModel.ssidState,
        passwordState = viewModel.passwordState,
        noteState = viewModel.noteState,
        onAction = viewModel::onAction,
    )
}
