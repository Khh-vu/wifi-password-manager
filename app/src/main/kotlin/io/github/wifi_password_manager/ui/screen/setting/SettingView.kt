package io.github.wifi_password_manager.ui.screen.setting

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.BuildConfig
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.LocalSettings
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.navigation.Route
import io.github.wifi_password_manager.ui.UiConfig
import io.github.wifi_password_manager.ui.icons.BugReport
import io.github.wifi_password_manager.ui.icons.Info
import io.github.wifi_password_manager.ui.icons.IntegrationInstructions
import io.github.wifi_password_manager.ui.icons.Language
import io.github.wifi_password_manager.ui.icons.Palette
import io.github.wifi_password_manager.ui.icons.Security
import io.github.wifi_password_manager.ui.icons.SwapVert
import io.github.wifi_password_manager.ui.icons.Tune
import io.github.wifi_password_manager.ui.screen.setting.components.AppLockItem
import io.github.wifi_password_manager.ui.screen.setting.components.ConnectedWifiInfoItem
import io.github.wifi_password_manager.ui.screen.setting.components.ExportDialog
import io.github.wifi_password_manager.ui.screen.setting.components.ForgetAllDialog
import io.github.wifi_password_manager.ui.screen.setting.components.ImportPasswordDialog
import io.github.wifi_password_manager.ui.screen.setting.components.LanguageItem
import io.github.wifi_password_manager.ui.screen.setting.components.PlaintextPasswordsItem
import io.github.wifi_password_manager.ui.screen.setting.components.SettingSection
import io.github.wifi_password_manager.ui.screen.setting.components.ThemeModeItem
import io.github.wifi_password_manager.ui.shared.BackButton
import io.github.wifi_password_manager.ui.shared.LoadingDialog
import io.github.wifi_password_manager.ui.theme.ThemeWrapper
import io.github.wifi_password_manager.utils.plus

@Composable
fun SettingView(state: SettingViewModel.State, onAction: (SettingViewModel.Action) -> Unit) {
    val uriHandler = LocalUriHandler.current
    val navBackStack = LocalNavBackStack.current
    val settings = LocalSettings.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton() },
                title = { Text(text = stringResource(R.string.settings_title)) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = innerPadding + PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Language Section
            item {
                SettingSection(
                    imageVector = Language,
                    title = stringResource(R.string.language_title),
                ) {
                    LanguageItem(language = settings.language) {
                        onAction(SettingViewModel.Action.UpdateLanguage(it))
                    }
                }
            }

            // Appearance Section
            item {
                SettingSection(
                    imageVector = Palette,
                    title = stringResource(R.string.appearance_section),
                ) {
                    ThemeModeItem(themeMode = settings.themeMode) {
                        onAction(SettingViewModel.Action.UpdateThemeMode(it))
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.background)

                        ListItem(
                            onClick = { onAction(SettingViewModel.Action.ToggleMaterialYou(!settings.useMaterialYou)) },
                            trailingContent = {
                                Switch(
                                    checked = settings.useMaterialYou,
                                    onCheckedChange = {
                                        onAction(SettingViewModel.Action.ToggleMaterialYou(it))
                                    },
                                )
                            },
                            shapes = UiConfig.listItemShapes(),
                            colors = UiConfig.listItemColors(),
                        ) {
                            Text(text = stringResource(R.string.material_you_title))
                        }
                    }

                    AnimatedVisibility(
                        visible = settings.themeMode.isDark,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column {
                            HorizontalDivider(color = MaterialTheme.colorScheme.background)

                            ListItem(
                                onClick = { onAction(SettingViewModel.Action.TogglePureBlackTheme(!settings.pureBlackTheme)) },
                                supportingContent = { Text(text = stringResource(R.string.pure_black_theme_description)) },
                                trailingContent = {
                                    Switch(
                                        checked = settings.pureBlackTheme,
                                        onCheckedChange = {
                                            onAction(SettingViewModel.Action.TogglePureBlackTheme(it))
                                        },
                                    )
                                },
                                shapes = UiConfig.listItemShapes(),
                                colors = UiConfig.listItemColors(),
                            ) {
                                Text(text = stringResource(R.string.pure_black_theme_title))
                            }
                        }
                    }
                }
            }

            // Import & Export Section
            item {
                SettingSection(
                    imageVector = SwapVert,
                    title = stringResource(R.string.import_export_section),
                ) {
                    ListItem(
                        onClick = { onAction(SettingViewModel.Action.ImportNetworks) },
                        supportingContent = { Text(text = stringResource(R.string.import_description)) },
                        shapes = UiConfig.listItemShapes(),
                        colors = UiConfig.listItemColors(),
                        enabled = state.mode.hasPrivilegedAccess,
                    ) {
                        Text(text = stringResource(R.string.import_action))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.background)

                    ListItem(
                        onClick = { onAction(SettingViewModel.Action.ShowExportDialog) },
                        supportingContent = { Text(text = stringResource(R.string.export_description)) },
                        shapes = UiConfig.listItemShapes(),
                        colors = UiConfig.listItemColors(),
                    ) {
                        Text(text = stringResource(R.string.export_action))
                    }
                }
            }

            // Security Section
            item {
                SettingSection(
                    imageVector = Security,
                    title = stringResource(R.string.security_section),
                ) {
                    AppLockItem(
                        appLockEnabled = settings.appLockEnabled,
                        onToggleAppLock = { onAction(SettingViewModel.Action.ToggleAppLock(it)) },
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.background)

                    ListItem(
                        onClick = { onAction(SettingViewModel.Action.ToggleSecureScreen(!settings.secureScreenEnabled)) },
                        supportingContent = { Text(text = stringResource(R.string.secure_screen_description)) },
                        trailingContent = {
                            Switch(
                                checked = settings.secureScreenEnabled,
                                onCheckedChange = {
                                    onAction(SettingViewModel.Action.ToggleSecureScreen(it))
                                },
                            )
                        },
                        shapes = UiConfig.listItemShapes(),
                        colors = UiConfig.listItemColors(),
                    ) {
                        Text(text = stringResource(R.string.secure_screen_title))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.background)

                    PlaintextPasswordsItem(
                        enabled = settings.secureScreenEnabled,
                        value = settings.plaintextPasswords,
                        onValueChange = {
                            onAction(SettingViewModel.Action.TogglePlaintextPasswords(it))
                        },
                    )
                }
            }

            // External Integration Section
            item {
                SettingSection(
                    imageVector = IntegrationInstructions,
                    title = stringResource(R.string.external_integration_section),
                ) {
                    ListItem(
                        onClick = { onAction(SettingViewModel.Action.ToggleAllowInsecureReceiver(!settings.allowInsecureReceiver)) },
                        supportingContent = { Text(text = stringResource(R.string.allow_insecure_receiver_description)) },
                        trailingContent = {
                            Switch(
                                checked = settings.allowInsecureReceiver,
                                onCheckedChange = {
                                    onAction(SettingViewModel.Action.ToggleAllowInsecureReceiver(it))
                                },
                            )
                        },
                        shapes = UiConfig.listItemShapes(),
                        colors = UiConfig.listItemColors(),
                    ) {
                        Text(text = stringResource(R.string.allow_insecure_receiver_title))
                    }

                    AnimatedVisibility(
                        visible = settings.allowInsecureReceiver,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column {
                            HorizontalDivider(color = MaterialTheme.colorScheme.background)

                            ListItem(
                                onClick = { navBackStack.add(Route.ExportWifiSetupScreen) },
                                shapes = UiConfig.listItemShapes(),
                                colors = UiConfig.listItemColors(),
                            ) {
                                Text(text = stringResource(R.string.export_wifi_action))
                            }
                        }
                    }
                }
            }

            // Advanced Section
            item {
                SettingSection(
                    imageVector = Tune,
                    title = stringResource(R.string.advanced_section),
                ) {
                    ListItem(
                        onClick = { onAction(SettingViewModel.Action.ShowForgetAllDialog) },
                        supportingContent = { Text(text = stringResource(R.string.forget_all_description)) },
                        shapes = UiConfig.listItemShapes(),
                        colors = UiConfig.listItemColors(),
                        enabled = state.mode.hasPrivilegedAccess,
                    ) {
                        Text(text = stringResource(R.string.forget_all_title))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.background)

                    ListItem(
                        onClick = {
                            onAction(SettingViewModel.Action.ToggleAutoPersistEphemeralNetworks(!settings.autoPersistEphemeralNetworks))
                        },
                        supportingContent = { Text(text = stringResource(R.string.auto_persist_ephemeral_networks_description)) },
                        trailingContent = {
                            Switch(
                                checked = settings.autoPersistEphemeralNetworks,
                                onCheckedChange = {
                                    onAction(
                                        SettingViewModel.Action.ToggleAutoPersistEphemeralNetworks(
                                            it
                                        )
                                    )
                                },
                            )
                        },
                        shapes = UiConfig.listItemShapes(),
                        colors = UiConfig.listItemColors(),
                    ) {
                        Text(text = stringResource(R.string.auto_persist_ephemeral_networks_title))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.background)

                    ListItem(
                        onClick = { onAction(SettingViewModel.Action.ToggleAllowCacheMode(!settings.allowCacheMode)) },
                        supportingContent = { Text(text = stringResource(R.string.allow_cache_mode_description)) },
                        trailingContent = {
                            Switch(
                                checked = settings.allowCacheMode,
                                onCheckedChange = {
                                    onAction(SettingViewModel.Action.ToggleAllowCacheMode(it))
                                },
                            )
                        },
                        shapes = UiConfig.listItemShapes(),
                        colors = UiConfig.listItemColors(),
                    ) {
                        Text(text = stringResource(R.string.allow_cache_mode_title))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.background)

                    ConnectedWifiInfoItem(mode = state.mode)
                }
            }

            // Debugging Section
            item {
                SettingSection(
                    imageVector = BugReport,
                    title = stringResource(R.string.debugging),
                ) {
                    ListItem(
                        onClick = { navBackStack.add(Route.IWifiManagerMethodInspectorScreen) },
                        shapes = UiConfig.listItemShapes(),
                        colors = UiConfig.listItemColors(),
                    ) {
                        Text(text = "IWifiManager Methods")
                    }
                }
            }

            // About Section
            item {
                SettingSection(
                    imageVector = Info,
                    title = stringResource(R.string.about_section),
                ) {
                    ListItem(
                        onClick = { navBackStack.add(Route.LicenseScreen) },
                        supportingContent = { Text(text = stringResource(R.string.license_description)) },
                        shapes = UiConfig.listItemShapes(),
                        colors = UiConfig.listItemColors(),
                    ) {
                        Text(text = stringResource(R.string.license_title))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.background)

                    ListItem(
                        onClick = { uriHandler.openUri(BuildConfig.SOURCE_CODE_URL) },
                        supportingContent = { Text(text = BuildConfig.SOURCE_CODE_URL) },
                        shapes = UiConfig.listItemShapes(),
                        colors = UiConfig.listItemColors(),
                    ) {
                        Text(text = stringResource(R.string.source_code_title))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.background)

                    ListItem(
                        supportingContent = { Text(text = BuildConfig.VERSION_NAME) },
                        shapes = UiConfig.listItemShapes(),
                        colors = UiConfig.listItemColors(),
                    ) {
                        Text(text = stringResource(R.string.version_title))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.background)

                    ListItem(
                        supportingContent = { Text(text = BuildConfig.BUILD_TYPE) },
                        shapes = UiConfig.listItemShapes(),
                        colors = UiConfig.listItemColors(),
                    ) {
                        Text(text = stringResource(R.string.build_type_title))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.background)

                    ListItem(
                        supportingContent = {
                            Column {
                                Text(text = "Brand: ${Build.BRAND}")
                                Text(text = "Model: ${Build.MODEL}")
                                Text(text = "Manufacturer: ${Build.MANUFACTURER}")
                                Text(text = "Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                            }
                        },
                        colors = UiConfig.listItemColors(),
                    ) {
                        Text(text = stringResource(R.string.about_device))
                    }
                }
            }
        }
    }

    when {
        state.isLoading -> {
            LoadingDialog()
        }

        state.showForgetAllDialog -> {
            ForgetAllDialog(
                onDismiss = { onAction(SettingViewModel.Action.HideForgetAllDialog) },
                onConfirm = { onAction(SettingViewModel.Action.ConfirmForgetAllNetworks(it)) },
            )
        }

        state.showExportDialog -> {
            ExportDialog(
                onDismiss = { onAction(SettingViewModel.Action.HideExportDialog) },
                onSelect = { option, password ->
                    onAction(SettingViewModel.Action.ConfirmExport(option, password))
                },
            )
        }

        state.showImportPasswordDialog -> {
            ImportPasswordDialog(
                onDismiss = { onAction(SettingViewModel.Action.HideImportPasswordDialog) },
                onConfirm = { password ->
                    onAction(SettingViewModel.Action.ConfirmImportWithPassword(password))
                },
            )
        }
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun SettingViewPreview() {
    SettingView(state = SettingViewModel.State(), onAction = {})
}

@PreviewScreenSizes
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun AdaptiveSettingViewPreview() {
    SettingView(state = SettingViewModel.State(), onAction = {})
}
