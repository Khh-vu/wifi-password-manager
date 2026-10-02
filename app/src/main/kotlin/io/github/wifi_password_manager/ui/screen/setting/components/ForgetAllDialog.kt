package io.github.wifi_password_manager.ui.screen.setting.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.icons.Warning
import io.github.wifi_password_manager.ui.theme.SurfaceWrapper

@Composable
fun ForgetAllDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit,
) {
    var clearCache by retain { mutableStateOf(false) }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Warning,
                contentDescription = stringResource(R.string.warning),
            )
        },
        title = { Text(text = stringResource(R.string.forget_all_confirmation_title)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Text(text = stringResource(R.string.forget_all_confirmation_message)) }

                item {
                    ListItem(
                        checked = clearCache,
                        onCheckedChange = { clearCache = it },
                        leadingContent = {
                            Checkbox(checked = clearCache, onCheckedChange = { clearCache = it })
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    ) {
                        Text(text = stringResource(R.string.forget_all_clear_cache))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(clearCache) }, shapes = ButtonDefaults.shapes()) {
                Text(text = stringResource(R.string.ok))
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
@PreviewWrapper(SurfaceWrapper::class)
private fun ForgetAllDialogPreview() {
    ForgetAllDialog(onDismiss = {}, onConfirm = {})
}
