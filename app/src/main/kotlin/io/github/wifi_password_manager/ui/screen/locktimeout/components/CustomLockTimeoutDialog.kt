package io.github.wifi_password_manager.ui.screen.locktimeout.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.theme.SurfaceWrapper
import io.github.wifi_password_manager.utils.title
import io.github.wifi_password_manager.utils.toValueAndUnit
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val units = listOf(DurationUnit.SECONDS, DurationUnit.MINUTES)

@Composable
fun CustomLockTimeoutDialog(
    initial: Duration,
    onDismiss: () -> Unit,
    onConfirm: (Duration) -> Unit,
) {
    val (initValue, initUnit) = (if (initial.isInfinite()) 30.seconds else initial).toValueAndUnit()
    val value = rememberTextFieldState(initialText = initValue.toString())
    var unit by retain { mutableStateOf(initUnit) }
    val duration = value.text.toString().toLongOrNull()?.toDuration(unit)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.lock_timeout_custom)) },
        text = {
            LazyColumn {
                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        state = value,
                        label = { Text(text = stringResource(R.string.lock_timeout_value)) },
                        lineLimits = TextFieldLineLimits.SingleLine,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        inputTransformation =
                            InputTransformation {
                                if (length > 2 || !asCharSequence().all { it.isDigit() }) {
                                    revertAllChanges()
                                }
                            },
                        trailingIcon = { UnitDropdown(unit = unit, onUnitChange = { unit = it }) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { duration?.let(onConfirm) },
                enabled = duration != null,
                shapes = ButtonDefaults.shapes(),
            ) {
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

@Composable
private fun UnitDropdown(
    modifier: Modifier = Modifier,
    unit: DurationUnit,
    onUnitChange: (DurationUnit) -> Unit,
) {
    var expanded by retain { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(onClick = { expanded = true }, shapes = ButtonDefaults.shapes()) {
            Text(text = unit.title)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            units.forEachIndexed { index, item ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))
                }

                SelectableDropdownMenuItem(
                    selected = item == unit,
                    onClick = {
                        onUnitChange(item)
                        expanded = false
                    },
                    text = { Text(text = item.title) },
                    shapes = MenuDefaults.itemShapes(),
                    colors =
                        MenuDefaults.selectableItemColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun CustomLockTimeoutDialogPreview() {
    CustomLockTimeoutDialog(
        initial = Random.nextInt(1..60).toDuration(units.random()),
        onDismiss = {},
        onConfirm = {},
    )
}
