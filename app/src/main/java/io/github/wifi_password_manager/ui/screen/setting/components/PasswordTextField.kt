package io.github.wifi_password_manager.ui.screen.setting.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.shared.TooltipIconButton
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    isError: Boolean = false,
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    var isObfuscated by remember { mutableStateOf(false) }
    val errorText = @Composable { Text(text = stringResource(R.string.password_required)) }

    OutlinedSecureTextField(
        state = state,
        modifier = modifier.fillMaxWidth().semantics { contentType = ContentType.Password },
        label = { Text(text = stringResource(R.string.password)) },
        supportingText = errorText.takeIf { isError },
        onKeyboardAction = onKeyboardAction,
        isError = isError,
        trailingIcon = {
            TooltipIconButton(
                onClick = { isObfuscated = !isObfuscated },
                imageVector =
                    if (isObfuscated) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                tooltip =
                    if (isObfuscated) stringResource(R.string.hide_password)
                    else stringResource(R.string.show_password),
                positioning = TooltipAnchorPosition.Above,
            )
        },
        textObfuscationMode =
            if (isObfuscated) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
    )
}

@PreviewLightDark
@Composable
private fun PasswordTextFieldPreview() {
    WiFiPasswordManagerTheme { Surface { PasswordTextField(state = rememberTextFieldState()) } }
}
