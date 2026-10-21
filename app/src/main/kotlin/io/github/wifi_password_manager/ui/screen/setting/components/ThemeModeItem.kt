package io.github.wifi_password_manager.ui.screen.setting.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.Settings
import io.github.wifi_password_manager.ui.UiConfig
import io.github.wifi_password_manager.ui.theme.SurfaceWrapper
import io.github.wifi_password_manager.ui.theme.ThemeWrapper

@Composable
fun ThemeModeItem(themeMode: Settings.ThemeMode, onThemeModeChange: (Settings.ThemeMode) -> Unit) {
    var showBottomSheet by retain { mutableStateOf(false) }

    ListItem(
        onClick = { showBottomSheet = true },
        supportingContent = { Text(text = stringResource(R.string.app_theme_description)) },
        trailingContent = { Text(text = stringResource(themeMode.resId)) },
        shapes = UiConfig.listItemShapes(),
        colors = UiConfig.listItemColors(),
    ) {
        Text(text = stringResource(R.string.app_theme_title))
    }

    if (showBottomSheet) {
        ThemeModeSelectionSheet(
            onDismiss = { showBottomSheet = false },
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModeSelectionSheet(
    onDismiss: () -> Unit,
    themeMode: Settings.ThemeMode,
    onThemeModeChange: (Settings.ThemeMode) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Settings.ThemeMode.entries.forEach {
                SegmentedListItem(
                    onClick = { onThemeModeChange(it) },
                    selected = it == themeMode,
                    leadingContent = { RadioButton(selected = it == themeMode, onClick = null) },
                    shapes =
                        ListItemDefaults.segmentedShapes(
                            index = it.ordinal,
                            count = Settings.ThemeMode.entries.size,
                        ),
                    colors =
                        ListItemDefaults.segmentedColors(
                            containerColor = BottomSheetDefaults.ContainerColor
                        ),
                ) {
                    Text(text = stringResource(it.resId))
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun ThemeModeItemPreview() {
    ThemeModeItem(themeMode = Settings.ThemeMode.SYSTEM, onThemeModeChange = {})
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun ThemeModeSelectionSheetPreview() {
    ThemeModeSelectionSheet(
        onDismiss = {},
        themeMode = Settings.ThemeMode.entries.random(),
        onThemeModeChange = {},
    )
}
