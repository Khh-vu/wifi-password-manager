package io.github.wifi_password_manager.ui.shared

import android.content.ClipData
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.BuildConfig
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.PrivilegedMode
import io.github.wifi_password_manager.ui.theme.SurfaceWrapper
import kotlin.random.Random
import kotlinx.coroutines.launch

@Composable
fun MethodSignatureErrorDialog(
    modifier: Modifier = Modifier,
    report: String,
    onDismiss: () -> Unit,
) {
    val resources = LocalResources.current
    val clipboard = LocalClipboard.current

    val scope = rememberCoroutineScope()

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.method_inspector_error_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(R.string.method_inspector_error_message))
                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    item {
                        SelectionContainer {
                            Text(
                                text = report,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val label = resources.getString(R.string.method_inspector_error_title)
                    scope.launch {
                        clipboard.setClipEntry(ClipData.newPlainText(label, report).toClipEntry())
                    }
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(text = stringResource(R.string.copy_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text(text = stringResource(R.string.ok))
            }
        },
    )
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun MethodSignatureErrorDialogPreview() {
    MethodSignatureErrorDialog(
        report =
            buildString {
                appendLine("App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                appendLine("Build: ${Build.FINGERPRINT}")
                appendLine(
                    "Mode: ${if(Random.nextBoolean()) PrivilegedMode.ROOT else PrivilegedMode.SHIZUKU}",
                )

                appendLine()

                appendLine("IWifiManager methods on this device:")

                appendLine()

                appendLine("Error:")
                append(
                    "java.lang.NoSuchMethodError: ${LoremIpsum(10).values.joinToString(" ")}",
                )
            },
        onDismiss = {},
    )
}
