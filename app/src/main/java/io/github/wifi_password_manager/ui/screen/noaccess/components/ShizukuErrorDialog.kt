package io.github.wifi_password_manager.ui.screen.noaccess.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import io.github.wifi_password_manager.utils.launchUrl
import rikka.shizuku.ShizukuProvider

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShizukuErrorDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    val isShizukuInstalled = remember {
        try {
            context.packageManager.getApplicationInfo(ShizukuProvider.MANAGER_APPLICATION_ID, 0)
            true
        } catch (_: Throwable) {
            false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.shizuku_required_title)) },
        text = { Text(text = stringResource(R.string.shizuku_required_message)) },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isShizukuInstalled) {
                        context.startActivity(
                            context.packageManager.getLaunchIntentForPackage(
                                ShizukuProvider.MANAGER_APPLICATION_ID
                            )
                        )
                    } else {
                        context.launchUrl("https://shizuku.rikka.app/download/")
                    }
                    onDismiss()
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(
                    text =
                        stringResource(
                            if (isShizukuInstalled) R.string.open_shizuku
                            else R.string.install_shizuku
                        )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun ShizukuErrorDialogPreview() {
    WiFiPasswordManagerTheme { ShizukuErrorDialog(onDismiss = {}) }
}
