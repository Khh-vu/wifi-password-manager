package io.github.wifi_password_manager.ui.screen.methodinspector

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.wifi_password_manager.ui.shared.ObserveAsEvent
import io.github.wifi_password_manager.utils.toast
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.reflect.KClass

@Composable
fun <T : Any> MethodInspectorScreen(forClass: KClass<T>) {
    val context = LocalContext.current
    val resources = LocalResources.current

    val viewModel = koinViewModel<MethodInspectorViewModel<T>> { parametersOf(forClass) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvent(viewModel.event) { event ->
        when (event) {
            is MethodInspectorViewModel.Event.ShowMessage -> {
                context.toast(event.message.asString(resources))
            }
        }
    }

    MethodInspectorView(state = state, searchState = viewModel.searchState, onAction = viewModel::onAction)
}