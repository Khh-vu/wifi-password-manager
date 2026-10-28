package io.github.wifi_password_manager.ui.screen.noaccess

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.topjohnwu.superuser.Shell
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.icons.Lock
import io.github.wifi_password_manager.ui.screen.noaccess.components.ShizukuErrorDialog
import io.github.wifi_password_manager.ui.theme.ThemeWrapper
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

private const val REQUEST_CODE = 10001

@Composable
fun NoAccessView(
    allowSkip: Boolean,
    onSkip: () -> Unit,
    onGranted: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var showErrorDialog by retain { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
            granted ->
            if (!granted) {
                showErrorDialog = true
                return@rememberLauncherForActivityResult
            }
            onGranted()
        }

    val shizukuPermissionListener =
        object : Shizuku.OnRequestPermissionResultListener {
            override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                if (requestCode != REQUEST_CODE) return

                Shizuku.removeRequestPermissionResultListener(this)
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    onGranted()
                } else {
                    showErrorDialog = true
                }
            }
        }

    fun requestAccess() {
        scope.launch {
            val isRoot = runCatching {
                withContext(Shell.EXECUTOR.asCoroutineDispatcher()) { Shell.getShell().isRoot }
            }
                .getOrElse { false }

            if (isRoot) {
                onGranted()
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

    Surface(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Icon(
                    imageVector = Lock,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.no_access_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.no_access_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                FlowRow(horizontalArrangement = Arrangement.Center) {
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
@PreviewWrapper(ThemeWrapper::class)
private fun NoAccessViewPreview() {
    NoAccessView(
        allowSkip = false,
        onSkip = {},
        onGranted = {},
    )
}

@PreviewScreenSizes
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun AdaptiveNoAccessViewPreview() {
    NoAccessView(
        allowSkip = false,
        onSkip = {},
        onGranted = {},
    )
}
