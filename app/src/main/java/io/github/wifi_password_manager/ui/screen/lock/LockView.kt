package io.github.wifi_password_manager.ui.screen.lock

import androidx.biometric.AuthenticationRequest
import androidx.biometric.AuthenticationResult
import androidx.biometric.BiometricPrompt
import androidx.biometric.compose.rememberAuthenticationLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import io.github.wifi_password_manager.utils.toast

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LockView(onAuthenticated: () -> Unit) {
    val context = LocalContext.current
    val resources = LocalResources.current

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
                onAuthenticated()
            }
        }
    }
    val startAuthentication by rememberUpdatedState {
        val request =
            AuthenticationRequest.biometricRequest(
                title = resources.getString(R.string.app_lock_required),
                authFallback = AuthenticationRequest.Biometric.Fallback.DeviceCredential,
            ) {}
        launcher.launch(request)
    }

    LaunchedEffect(Unit) { startAuthentication() }

    Surface(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = stringResource(R.string.lock_description),
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.lock_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.app_lock_required),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            TextButton(
                onClick = startAuthentication,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Text(text = stringResource(R.string.unlock))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LockViewPreview() {
    WiFiPasswordManagerTheme { LockView(onAuthenticated = {}) }
}

@PreviewScreenSizes
@Composable
private fun AdaptiveLockViewPreview() {
    WiFiPasswordManagerTheme { LockView(onAuthenticated = {}) }
}
