package io.github.wifi_password_manager.ui.screen.setting.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.icons.Visibility
import io.github.wifi_password_manager.ui.icons.VisibilityOff
import io.github.wifi_password_manager.ui.shared.TooltipIconButton
import io.github.wifi_password_manager.ui.theme.SurfaceWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    isError: Boolean = false,
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    var isObfuscated by retain { mutableStateOf(false) }
    val errorText = @Composable { Text(text = stringResource(R.string.password_required)) }

    OutlinedSecureTextField(
        state = state,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentType = ContentType.Password },
        label = { Text(text = stringResource(R.string.password)) },
        supportingText = errorText.takeIf { isError },
        onKeyboardAction = onKeyboardAction,
        isError = isError,
        trailingIcon = {
            TooltipIconButton(
                onClick = { isObfuscated = !isObfuscated },
                imageVector = if (isObfuscated) VisibilityOff else Visibility,
                tooltip = if (isObfuscated) {
                    stringResource(R.string.hide_password)
                } else {
                    stringResource(R.string.show_password)
                },
                positioning = TooltipAnchorPosition.Above,
            )
        },
        textObfuscationMode = if (isObfuscated) TextObfuscationMode.Visible else TextObfuscationMode.System,
    )
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun PasswordTextFieldPreview() {
    PasswordTextField(state = rememberTextFieldState())
}
