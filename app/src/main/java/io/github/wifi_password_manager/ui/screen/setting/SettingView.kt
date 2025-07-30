package io.github.wifi_password_manager.ui.screen.setting

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.data.Settings
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.ui.screen.setting.components.ThemeModeItem
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingView(state: Settings, onEvent: (SettingViewModel.Event) -> Unit) {
    val navBackStack = LocalNavBackStack.current

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navBackStack.removeLastOrNull() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                title = { Text(text = "Settings") },
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                text = "Appearance",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp),
            )

            ThemeModeItem(themeMode = state.themeMode) {
                onEvent(SettingViewModel.Event.UpdateThemeMode(it))
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ListItem(
                    modifier =
                        Modifier.clickable {
                            onEvent(SettingViewModel.Event.ToggleMaterialYou(!state.useMaterialYou))
                        },
                    headlineContent = { Text(text = "Material You") },
                    trailingContent = {
                        Switch(
                            checked = state.useMaterialYou,
                            onCheckedChange = {
                                onEvent(SettingViewModel.Event.ToggleMaterialYou(it))
                            },
                        )
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun SettingViewPreview() {
    WiFiPasswordManagerTheme { SettingView(state = Settings(), onEvent = {}) }
}
