package io.github.wifi_password_manager.ui.screen.network.list

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.shared.ObserveAsEvent
import io.github.wifi_password_manager.utils.toast
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NetworkListScreen() {
    val context = LocalContext.current
    val resources = LocalResources.current

    val viewModel = koinViewModel<NetworkListViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvent(viewModel.event) { event ->
        when (event) {
            is NetworkListViewModel.Event.ShowMessage -> {
                context.toast(event.message.asString(resources))
            }
        }
    }

    NetworkListView(
        state = state,
        searchState = viewModel.searchState,
        onAction = viewModel::onAction,
        onScanQr = {
            try {
                context.startActivity(Intent("android.settings.WIFI_DPP_ENROLLEE_QR_CODE_SCANNER"))
            } catch (_: ActivityNotFoundException) {
                context.toast(R.string.scan_wifi_qr_unsupported)
            }
        },
    )
}
