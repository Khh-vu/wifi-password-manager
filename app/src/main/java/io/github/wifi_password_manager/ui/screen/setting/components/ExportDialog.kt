package io.github.wifi_password_manager.ui.screen.setting.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.ExportOption
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExportDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSelect: (ExportOption) -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(R.string.export_action)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ExportOption.entries) { option ->
                    ListItem(
                        onClick = { onSelect(option) },
                        content = { Text(text = stringResource(option.titleResId)) },
                        supportingContent = { Text(text = stringResource(option.descriptionResId)) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss() }, shapes = ButtonDefaults.shapes()) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun ExportDialogPreview() {
    WiFiPasswordManagerTheme { ExportDialog(onDismiss = {}, onSelect = {}) }
}
