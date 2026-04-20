package io.github.wifi_password_manager.ui.screen.noaccess

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.topjohnwu.superuser.Shell
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.manager.PrivilegedManager
import io.github.wifi_password_manager.ui.screen.noaccess.components.ShizukuErrorDialog
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

private const val REQUEST_CODE = 10001

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NoAccessView(allowSkip: Boolean, onAccessGranted: () -> Unit, onSkip: () -> Unit) {
    val privilegedManager = koinInject<PrivilegedManager>()

    val scope = rememberCoroutineScope()
    var showErrorDialog by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
            granted ->
            if (!granted) {
                showErrorDialog = true
                return@rememberLauncherForActivityResult
            }
            scope.launch {
                privilegedManager.refresh()
                if (privilegedManager.currentMode.hasPrivilegedAccess) {
                    onAccessGranted()
                }
            }
        }

    val shizukuPermissionListener =
        object : Shizuku.OnRequestPermissionResultListener {
            override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                if (requestCode != REQUEST_CODE) return

                Shizuku.removeRequestPermissionResultListener(this)
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    scope.launch {
                        privilegedManager.refresh()
                        if (privilegedManager.currentMode.hasPrivilegedAccess) {
                            onAccessGranted()
                        }
                    }
                } else {
                    showErrorDialog = true
                }
            }
        }

    fun requestAccess() {
        scope.launch {
            val isRoot =
                runCatching {
                        withContext(Shell.EXECUTOR.asCoroutineDispatcher()) {
                            Shell.getShell().isRoot
                        }
                    }
                    .getOrElse { false }

            if (isRoot) {
                privilegedManager.refresh()
                onAccessGranted()
                return@launch
            }

            try {
                if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
                    permissionLauncher.launch(ShizukuProvider.PERMISSION)
                } else {
                    Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
                    Shizuku.requestPermission(REQUEST_CODE)
                }
            } catch (_: Throwable) {
                showErrorDialog = true
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.no_access_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.no_access_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.Center) {
                    if (allowSkip) {
                        OutlinedButton(onClick = onSkip, shapes = ButtonDefaults.shapes()) {
                            Text(text = stringResource(R.string.continue_in_cache_mode))
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(onClick = ::requestAccess, shapes = ButtonDefaults.shapes()) {
                        Text(text = stringResource(R.string.grant_access))
                    }
                }
            }
        }
    }

    if (showErrorDialog) {
        ShizukuErrorDialog(onDismiss = { showErrorDialog = false })
    }
}

@PreviewLightDark
@Composable
private fun NoAccessViewPreview() {
    WiFiPasswordManagerTheme { NoAccessView(allowSkip = false, onAccessGranted = {}, onSkip = {}) }
}

@PreviewScreenSizes
@Composable
private fun AdaptiveNoAccessViewPreview() {
    WiFiPasswordManagerTheme { NoAccessView(allowSkip = false, onAccessGranted = {}, onSkip = {}) }
}
