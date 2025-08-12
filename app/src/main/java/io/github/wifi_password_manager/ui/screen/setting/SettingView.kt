package io.github.wifi_password_manager.ui.screen.setting

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.ui.screen.setting.components.ForgetAllNetworksItem
import io.github.wifi_password_manager.ui.screen.setting.components.ThemeModeItem
import io.github.wifi_password_manager.ui.shared.LoadingDialog
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingView(state: SettingViewModel.State, onEvent: (SettingViewModel.Event) -> Unit) {
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
                title = { Text(text = stringResource(R.string.settings_title)) },
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Appearance Section
            Text(
                text = stringResource(R.string.appearance_section),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            ThemeModeItem(themeMode = state.settings.themeMode) {
                onEvent(SettingViewModel.Event.UpdateThemeMode(it))
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ListItem(
                    modifier =
                        Modifier.clickable {
                            onEvent(
                                SettingViewModel.Event.ToggleMaterialYou(
                                    !state.settings.useMaterialYou
                                )
                            )
                        },
                    headlineContent = { Text(text = stringResource(R.string.material_you_title)) },
                    trailingContent = {
                        Switch(
                            checked = state.settings.useMaterialYou,
                            onCheckedChange = {
                                onEvent(SettingViewModel.Event.ToggleMaterialYou(it))
                            },
                        )
                    },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // Import & Export Section
            Text(
                text = stringResource(R.string.import_export_section),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            ListItem(
                modifier = Modifier.clickable { onEvent(SettingViewModel.Event.ImportNetworks) },
                headlineContent = { Text(text = stringResource(R.string.import_action)) },
                supportingContent = { Text(text = stringResource(R.string.import_description)) },
            )

            ListItem(
                modifier = Modifier.clickable { onEvent(SettingViewModel.Event.ExportNetworks) },
                headlineContent = { Text(text = stringResource(R.string.export_action)) },
                supportingContent = { Text(text = stringResource(R.string.export_description)) },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // Advanced Section
            Text(
                text = stringResource(R.string.advanced_section),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            ForgetAllNetworksItem { onEvent(SettingViewModel.Event.ForgetAllNetworks) }
        }

        if (state.isLoading) {
            LoadingDialog()
        }
    }
}

@Preview
@Composable
private fun SettingViewPreview() {
    WiFiPasswordManagerTheme { SettingView(state = SettingViewModel.State(), onEvent = {}) }
}
