package io.github.wifi_password_manager.ui.screen.note

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.ui.shared.ObserveAsEvent
import io.github.wifi_password_manager.utils.toast
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NoteScreen(network: WifiNetwork) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val navBackStack = LocalNavBackStack.current

    val viewModel = koinViewModel<NoteViewModel> { parametersOf(network) }

    ObserveAsEvent(viewModel.event) { event ->
        when (event) {
            is NoteViewModel.Event.ShowMessage -> {
                context.toast(event.message.asString(resources))
            }

            is NoteViewModel.Event.NavigateBack -> {
                navBackStack.removeLastOrNull()
            }
        }
    }

    NoteView(
        network = network,
        noteState = viewModel.noteState,
        onAction = viewModel::onAction,
    )
}
