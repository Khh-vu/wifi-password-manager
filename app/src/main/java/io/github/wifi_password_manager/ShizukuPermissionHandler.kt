package io.github.wifi_password_manager

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.wifi_password_manager.utils.hasShizukuPermission
import io.github.wifi_password_manager.utils.launchUrl
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

@Composable
fun ShizukuPermissionHandler(finishCallback: () -> Unit, content: @Composable () -> Unit) {
    val context = LocalContext.current

    var isShizukuAvailable by remember { mutableStateOf(context.hasShizukuPermission) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
            isGranted ->
            isShizukuAvailable = context.hasShizukuPermission
            showErrorDialog = !isGranted
        }

    fun requestShizukuPermission() {
        try {
            if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
                permissionLauncher.launch(ShizukuProvider.PERMISSION)
            } else {
                Shizuku.requestPermission(10001)
            }
        } catch (_: Throwable) {
            showErrorDialog = true
        }
    }

    DisposableEffect(Unit) {
        if (!isShizukuAvailable) {
            requestShizukuPermission()
        }

        val permissionResultListener =
            object : Shizuku.OnRequestPermissionResultListener {
                override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                    Shizuku.removeRequestPermissionResultListener(this)
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        isShizukuAvailable = context.hasShizukuPermission
                    } else {
                        showErrorDialog = true
                    }
                }
            }

        Shizuku.addRequestPermissionResultListener(permissionResultListener)
        onDispose { Shizuku.removeRequestPermissionResultListener(permissionResultListener) }
    }

    if (isShizukuAvailable) {
        content()
    }

    if (showErrorDialog) {
        ShizukuErrorDialog(onDismiss = { finishCallback() })
    }
}

@Composable
private fun ShizukuErrorDialog(onDismiss: () -> Unit) {
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
                }
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
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.cancel)) }
        },
    )
}
