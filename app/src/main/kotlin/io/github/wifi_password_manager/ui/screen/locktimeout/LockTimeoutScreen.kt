package io.github.wifi_password_manager.ui.screen.locktimeout

import androidx.compose.runtime.Composable
import io.github.wifi_password_manager.domain.model.LocalSettings
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LockTimeoutScreen() {
    val viewModel = koinViewModel<LockTimeoutViewModel>()

    LockTimeoutView(
        lockTimeout = LocalSettings.current.lockTimeout,
        onLockTimeoutChange = {
            viewModel.onAction(LockTimeoutViewModel.Action.UpdateLockTimeout(it))
        },
    )
}
