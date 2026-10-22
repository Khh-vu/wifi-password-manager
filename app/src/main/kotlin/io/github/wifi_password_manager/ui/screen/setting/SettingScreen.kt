package io.github.wifi_password_manager.ui.screen.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.wifi_password_manager.ui.shared.ObserveAsEvent
import io.github.wifi_password_manager.utils.setAppTheme
import io.github.wifi_password_manager.utils.toast
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingScreen() {
    val context = LocalContext.current
    val resources = LocalResources.current

    val viewModel = koinViewModel<SettingViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvent(viewModel.event) { event ->
        when (event) {
            is SettingViewModel.Event.ShowMessage -> {
                context.toast(event.message.asString(resources))
            }

            is SettingViewModel.Event.UpdateThemeMode -> {
                context.setAppTheme(event.themeMode)
            }
        }
    }

    SettingView(state = state, onAction = viewModel::onAction)
}
