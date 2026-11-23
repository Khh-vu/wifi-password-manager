package io.github.wifi_password_manager.ui.screen.setting.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.Settings
import io.github.wifi_password_manager.ui.UiConfig
import io.github.wifi_password_manager.ui.theme.SurfaceWrapper
import io.github.wifi_password_manager.ui.theme.ThemeWrapper

private val Settings.Language.displayName: String
    @Composable
    @ReadOnlyComposable
    get() =
        when (this) {
            Settings.Language.SYSTEM -> stringResource(R.string.language_system_default)
            Settings.Language.ENGLISH -> "English"
            Settings.Language.RUSSIAN -> "Русский"
            Settings.Language.CHINESE_SIMPLIFIED -> "中文 (简体)"
            Settings.Language.JAPANESE -> "日本語"
            Settings.Language.TURKISH -> "Türkçe"
            Settings.Language.ARABIC -> "العربية"
        }

@Composable
fun LanguageItem(language: Settings.Language, onLanguageChange: (Settings.Language) -> Unit) {
    var showDialog by retain { mutableStateOf(false) }

    ListItem(
        onClick = { showDialog = true },
        supportingContent = { Text(text = stringResource(R.string.language_description)) },
        trailingContent = { Text(text = language.displayName) },
        shapes = UiConfig.listItemShapes(),
        colors = UiConfig.listItemColors(),
    ) {
        Text(text = stringResource(R.string.language_title))
    }

    if (showDialog) {
        LanguageSelectionDialog(
            onDismiss = { showDialog = false },
            language = language,
            onLanguageChange = {
                showDialog = false
                onLanguageChange(it)
            },
        )
    }
}

@Composable
private fun LanguageSelectionDialog(
    onDismiss: () -> Unit,
    language: Settings.Language,
    onLanguageChange: (Settings.Language) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.language_title)) },
        text = {
            LazyColumn {
                items(
                    items = Settings.Language.entries.sortedBy { it.code },
                    key = { it.code },
                ) { lang ->
                    ListItem(
                        onClick = { onLanguageChange(lang) },
                        selected = lang == language,
                        leadingContent = {
                            RadioButton(selected = lang == language, onClick = null)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    ) {
                        Text(text = lang.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun LanguageItemPreview() {
    LanguageItem(language = Settings.Language.ENGLISH, onLanguageChange = {})
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun LanguageSelectionDialogPreview() {
    LanguageSelectionDialog(
        onDismiss = {},
        language = Settings.Language.entries.random(),
        onLanguageChange = {},
    )
}
