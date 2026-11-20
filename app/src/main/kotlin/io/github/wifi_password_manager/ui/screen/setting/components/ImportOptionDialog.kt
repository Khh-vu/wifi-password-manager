package io.github.wifi_password_manager.ui.screen.setting.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.theme.SurfaceWrapper

@Composable
fun ImportOptionDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSelect: (toDevice: Boolean) -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.import_mode_title)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                item {
                    SegmentedListItem(
                        onClick = { onSelect(true) },
                        selected = false,
                        supportingContent = {
                            Text(text = stringResource(R.string.import_to_device_description))
                        },
                        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
                    ) {
                        Text(text = stringResource(R.string.import_to_device))
                    }
                }

                item {
                    SegmentedListItem(
                        onClick = { onSelect(false) },
                        selected = false,
                        supportingContent = {
                            Text(text = stringResource(R.string.import_to_app_description))
                        },
                        shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
                    ) {
                        Text(text = stringResource(R.string.import_to_app))
                    }
                }
            }
        },
        confirmButton = {},
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
private fun ImportOptionDialogPreview() {
    ImportOptionDialog(onDismiss = {}, onSelect = {})
}
