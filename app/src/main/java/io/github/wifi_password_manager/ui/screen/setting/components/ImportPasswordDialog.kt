package io.github.wifi_password_manager.ui.screen.setting.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImportPasswordDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val autofillManager = LocalAutofillManager.current

    var showPasswordError by remember { mutableStateOf(false) }
    val password = rememberTextFieldState()

    AlertDialog(
        modifier = modifier,
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(R.string.import_password_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.import_password_description))

                PasswordTextField(
                    state = password,
                    isError = showPasswordError,
                    onKeyboardAction = {
                        showPasswordError = password.text.isBlank()
                        if (!showPasswordError) {
                            autofillManager?.commit()
                            onConfirm(password.text.toString())
                        }
                    },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    showPasswordError = password.text.isBlank()
                    if (!showPasswordError) {
                        autofillManager?.commit()
                        onConfirm(password.text.toString())
                    }
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(text = stringResource(R.string.ok))
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
private fun ImportPasswordDialogPreview() {
    WiFiPasswordManagerTheme { ImportPasswordDialog(onDismiss = {}, onConfirm = {}) }
}
