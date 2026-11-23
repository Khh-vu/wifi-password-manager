package io.github.wifi_password_manager.ui.screen.network.list.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.ExportOption
import io.github.wifi_password_manager.ui.shared.PasswordTextField
import io.github.wifi_password_manager.ui.theme.SurfaceWrapper

@Composable
fun ExportDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSelect: (ExportOption, String) -> Unit,
) {
    val autofillManager = LocalAutofillManager.current
    val resources = LocalResources.current

    var expanded by retain { mutableStateOf(false) }
    var encrypted by retain { mutableStateOf(false) }
    var selectedOption by retain { mutableStateOf(ExportOption.entries.first()) }
    var showPasswordError by retain { mutableStateOf(false) }

    val formatState = rememberTextFieldState()
    val passwordState = rememberTextFieldState()

    LaunchedEffect(selectedOption) {
        formatState.setTextAndPlaceCursorAtEnd(resources.getString(selectedOption.titleResId))
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.export_action)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        state = formatState,
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
                        containerColor = MaterialTheme.colorScheme.background,
                    ) {
                        ExportOption.entries.forEach { option ->
                            if (ExportOption.entries.first() != option) {
                                Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))
                            }

                            SelectableDropdownMenuItem(
                                selected = option == selectedOption,
                                onClick = {
                                    selectedOption = option
                                    expanded = false
                                },
                                text = { Text(text = stringResource(option.titleResId)) },
                                shapes = MenuDefaults.itemShapes(),
                                colors =
                                    MenuDefaults.selectableItemColors(
                                        selectedContainerColor =
                                            MaterialTheme.colorScheme.primaryContainer,
                                        selectedTextColor =
                                            contentColorFor(
                                                MaterialTheme.colorScheme.primaryContainer,
                                            ),
                                    ),
                            )
                        }
                    }
                }

                ListItem(
                    checked = encrypted,
                    onCheckedChange = { encrypted = it },
                    leadingContent = {
                        Checkbox(checked = encrypted, onCheckedChange = { encrypted = it })
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                ) {
                    Text(text = stringResource(R.string.encrypt_export))
                }

                AnimatedVisibility(visible = encrypted, modifier = Modifier.fillMaxWidth()) {
                    PasswordTextField(
                        state = passwordState,
                        isError = showPasswordError,
                        onKeyboardAction = {
                            showPasswordError = encrypted && passwordState.text.isBlank()
                            if (!showPasswordError) {
                                autofillManager?.commit()
                                onSelect(selectedOption, passwordState.text.toString())
                            }
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    showPasswordError = encrypted && passwordState.text.isBlank()
                    if (!showPasswordError) {
                        autofillManager?.commit()
                        onSelect(selectedOption, passwordState.text.toString())
                    }
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    autofillManager?.cancel()
                    onDismiss()
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun ExportDialogPreview() {
    ExportDialog(onDismiss = {}, onSelect = { _, _ -> })
}
