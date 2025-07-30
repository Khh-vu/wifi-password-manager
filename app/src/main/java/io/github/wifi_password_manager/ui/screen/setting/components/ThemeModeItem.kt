package io.github.wifi_password_manager.ui.screen.setting.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.wifi_password_manager.data.Settings
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeModeItem(
    modifier: Modifier = Modifier,
    themeMode: Settings.ThemeMode,
    onThemeModeChange: (Settings.ThemeMode) -> Unit,
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    ListItem(
        modifier = modifier.clickable { showBottomSheet = true },
        headlineContent = { Text(text = "App Theme") },
        supportingContent = { Text(text = "Change the app's appearance") },
        trailingContent = {
            Text(text = themeMode.name.lowercase().replaceFirstChar { it.uppercase() })
        },
    )

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
            Settings.ThemeMode.entries.forEach {
                ListItem(
                    modifier = Modifier.clickable { onThemeModeChange(it) },
                    leadingContent = {
                        RadioButton(selected = it == themeMode, onClick = { onThemeModeChange(it) })
                    },
                    headlineContent = { Text(text = it.name) },
                    colors =
                        ListItemDefaults.colors(containerColor = BottomSheetDefaults.ContainerColor),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ThemeModeItemPreview() {
    WiFiPasswordManagerTheme {
        ThemeModeItem(themeMode = Settings.ThemeMode.SYSTEM, onThemeModeChange = {})
    }
}
