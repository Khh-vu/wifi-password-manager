package io.github.wifi_password_manager.ui.screen.setting.components

import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.UiConfig
import io.github.wifi_password_manager.ui.theme.ThemeWrapper

@Composable
fun PlaintextPasswordsItem(
    enabled: Boolean = true,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    val overlineContent =
        @Composable {
            Text(
                text = stringResource(R.string.require_secure_screen),
                color = ListItemDefaults.contentColor,
            )
        }
    ListItem(
        enabled = enabled,
        onClick = { onValueChange(!value) },
        overlineContent = overlineContent.takeUnless { enabled },
        supportingContent = {
            Text(text = stringResource(R.string.plaintext_passwords_description))
        },
        trailingContent = {
            Switch(enabled = enabled, checked = value, onCheckedChange = onValueChange)
        },
        shapes = UiConfig.listItemShapes(),
        colors = UiConfig.listItemColors(),
    ) {
        Text(text = stringResource(R.string.plaintext_passwords_title))
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun EnabledPlaintextPasswordsItemPreview() {
    PlaintextPasswordsItem(value = true, onValueChange = {})
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun PlaintextPasswordsItemPreview() {
    PlaintextPasswordsItem(enabled = false, value = false, onValueChange = {})
}
