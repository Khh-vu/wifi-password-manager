package io.github.wifi_password_manager.ui.screen.setting.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.wifi_password_manager.R

@Composable
fun ForgetAllNetworksItem(modifier: Modifier = Modifier, onConfirm: () -> Unit) {
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    ListItem(
        modifier = modifier.clickable { showConfirmationDialog = true },
        headlineContent = { Text(text = stringResource(R.string.forget_all_action)) },
        supportingContent = { Text(text = stringResource(R.string.forget_all_description)) },
    )

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = stringResource(R.string.warning),
                )
            },
            title = { Text(text = stringResource(R.string.forget_all_confirmation_title)) },
            text = { Text(text = stringResource(R.string.forget_all_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm()
                        showConfirmationDialog = false
                    }
                ) {
                    Text(text = stringResource(R.string.forget_all_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }
}
