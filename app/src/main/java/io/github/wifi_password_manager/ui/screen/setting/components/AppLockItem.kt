package io.github.wifi_password_manager.ui.screen.setting.components

import android.content.Intent
import android.provider.Settings
import androidx.biometric.AuthenticationRequest
import androidx.biometric.AuthenticationResult
import androidx.biometric.BiometricPrompt
import androidx.biometric.compose.rememberAuthenticationLauncher
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.UiConfig
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import io.github.wifi_password_manager.utils.isBiometricAuthenticationSupported
import io.github.wifi_password_manager.utils.toast

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppLockItem(
    modifier: Modifier = Modifier,
    appLockEnabled: Boolean,
    onToggleAppLock: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val isAvailable =
        if (LocalInspectionMode.current) false else context.isBiometricAuthenticationSupported()

    var showDialog by retain { mutableStateOf(false) }

    val appLockEnabledState by rememberUpdatedState(appLockEnabled)
    val launcher = rememberAuthenticationLauncher { result ->
        when (result) {
            is AuthenticationResult.Error -> {
                when (result.errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_TIMEOUT,
                    BiometricPrompt.ERROR_CANCELED -> Unit
                    BiometricPrompt.ERROR_LOCKOUT ->
                        context.toast(R.string.app_lock_too_many_attempts)
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT ->
                        context.toast(R.string.app_lock_permanently_locked)
                    else -> context.toast(R.string.app_lock_authentication_failed)
                }
            }
            is AuthenticationResult.Success -> {
                onToggleAppLock(!appLockEnabledState)
            }
        }
    }

    val handleToggle by rememberUpdatedState {
        val request =
            AuthenticationRequest.biometricRequest(
                title =
                    resources.getString(
                        if (appLockEnabledState) {
                            R.string.app_lock_disable_title
                        } else {
                            R.string.app_lock_enable_title
                        }
                    ),
                authFallback = AuthenticationRequest.Biometric.Fallback.DeviceCredential,
            ) {}
        launcher.launch(request)
    }

    ListItem(
        modifier = modifier,
        onClick = {
            if (isAvailable) {
                handleToggle()
            } else {
                showDialog = true
            }
        },
        content = { Text(text = stringResource(R.string.app_lock_title)) },
        supportingContent = { Text(text = stringResource(R.string.app_lock_description)) },
        trailingContent = {
            Switch(
                checked = appLockEnabled && isAvailable,
                onCheckedChange = {
                    if (isAvailable) {
                        handleToggle()
                    } else {
                        showDialog = true
                    }
                },
                enabled = isAvailable,
            )
        },
        shapes = UiConfig.listItemShapes(),
    )

    if (showDialog) {
        LockScreenRequiredDialog(onDismiss = { showDialog = false })
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LockScreenRequiredDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = stringResource(R.string.lock_screen_required_title),
            )
        },
        title = { Text(text = stringResource(R.string.lock_screen_required_title)) },
        text = { Text(text = stringResource(R.string.lock_screen_required_message)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(text = stringResource(R.string.open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }, shapes = ButtonDefaults.shapes()) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun AppLockItemPreview() {
    WiFiPasswordManagerTheme { AppLockItem(appLockEnabled = false, onToggleAppLock = {}) }
}

@PreviewLightDark
@Composable
private fun LockScreenRequiredDialogPreview() {
    WiFiPasswordManagerTheme { LockScreenRequiredDialog(onDismiss = {}) }
}
