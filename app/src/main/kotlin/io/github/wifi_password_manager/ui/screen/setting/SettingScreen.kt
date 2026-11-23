package io.github.wifi_password_manager.ui.screen.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.wifi_password_manager.ui.shared.ObserveAsEvent
import io.github.wifi_password_manager.utils.setAppTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingScreen() {
    val context = LocalContext.current

    val viewModel = koinViewModel<SettingViewModel>()
    val mode by viewModel.mode.collectAsStateWithLifecycle()

    ObserveAsEvent(viewModel.event) { event ->
        when (event) {
            is SettingViewModel.Event.UpdateThemeMode -> {
                context.setAppTheme(event.themeMode)
            }
        }
    }

    SettingView(mode = mode, onAction = viewModel::onAction)
}
