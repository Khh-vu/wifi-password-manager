package io.github.wifi_password_manager.ui.screen.setting.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.ExportOption
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSelect: (ExportOption, String) -> Unit,
) {
    val autofillManager = LocalAutofillManager.current

    var expanded by remember { mutableStateOf(false) }
    var encrypted by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(ExportOption.entries.first()) }
    var showPasswordError by remember { mutableStateOf(false) }
    val password = rememberTextFieldState()

    AlertDialog(
        modifier = modifier,
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(R.string.export_action)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        value = stringResource(selectedOption.titleResId),
                        onValueChange = {},
                        modifier =
                            Modifier.fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        readOnly = true,
                        label = { Text(text = stringResource(R.string.export_format)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        supportingText = {
                            Text(text = stringResource(selectedOption.descriptionResId))
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        ExportOption.entries.forEach { option ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedOption = option
                                    expanded = false
                                },
                                text = { Text(text = stringResource(option.titleResId)) },
                                shape = MenuDefaults.standaloneItemShape,
                            )
                        }
                    }
                }

                ListItem(
                    checked = encrypted,
                    onCheckedChange = { encrypted = it },
                    content = { Text(text = stringResource(R.string.encrypt_export)) },
                    leadingContent = {
                        Checkbox(checked = encrypted, onCheckedChange = { encrypted = it })
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )

                AnimatedVisibility(visible = encrypted, modifier = Modifier.fillMaxWidth()) {
                    PasswordTextField(
                        state = password,
                        isError = showPasswordError,
                        onKeyboardAction = {
                            showPasswordError = encrypted && password.text.isBlank()
                            if (!showPasswordError) {
                                autofillManager?.commit()
                                onSelect(selectedOption, password.text.toString())
                            }
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    showPasswordError = encrypted && password.text.isBlank()
                    if (!showPasswordError) {
                        autofillManager?.commit()
                        onSelect(selectedOption, password.text.toString())
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
private fun ExportDialogPreview() {
    WiFiPasswordManagerTheme { ExportDialog(onDismiss = {}, onSelect = { _, _ -> }) }
}
