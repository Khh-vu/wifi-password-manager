package io.github.wifi_password_manager.ui.screen.setting.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.Settings
import io.github.wifi_password_manager.ui.UiConfig
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LanguageItem(language: Settings.Language, onLanguageChange: (Settings.Language) -> Unit) {
    var showBottomSheet by retain { mutableStateOf(false) }

    ListItem(
        onClick = { showBottomSheet = true },
        content = { Text(text = stringResource(R.string.language_title)) },
        supportingContent = { Text(text = stringResource(R.string.language_description)) },
        trailingContent = { Text(text = language.displayName) },
        shapes = UiConfig.listItemShapes(),
    )

    if (showBottomSheet) {
        LanguageSelectionSheet(
            onDismiss = { showBottomSheet = false },
            language = language,
            onLanguageChange = onLanguageChange,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelectionSheet(
    onDismiss: () -> Unit,
    language: Settings.Language,
    onLanguageChange: (Settings.Language) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = { onDismiss() }) {
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Settings.Language.entries.forEach {
                ListItem(
                    onClick = { onLanguageChange(it) },
                    selected = it == language,
                    leadingContent = { RadioButton(selected = it == language, onClick = null) },
                    content = { Text(text = it.displayName) },
                    colors =
                        ListItemDefaults.colors(containerColor = BottomSheetDefaults.ContainerColor),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LanguageItemPreview() {
    WiFiPasswordManagerTheme {
        LanguageItem(language = Settings.Language.ENGLISH, onLanguageChange = {})
    }
}

@PreviewLightDark
@Composable
private fun LanguageSelectionSheetPreview() {
    WiFiPasswordManagerTheme {
        LanguageSelectionSheet(
            onDismiss = {},
            language = Settings.Language.entries.random(),
            onLanguageChange = {},
        )
    }
}
