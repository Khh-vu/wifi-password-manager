package io.github.wifi_password_manager.ui.screen.setting

import android.os.Build
import androidx.biometric.AuthenticationRequest
import androidx.biometric.AuthenticationResult
import androidx.biometric.BiometricPrompt
import androidx.biometric.compose.rememberAuthenticationLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.BuildConfig
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.navigation.Route
import io.github.wifi_password_manager.ui.screen.setting.components.ForgetAllConfirmDialog
import io.github.wifi_password_manager.ui.screen.setting.components.LanguageItem
import io.github.wifi_password_manager.ui.screen.setting.components.SettingSection
import io.github.wifi_password_manager.ui.screen.setting.components.ThemeModeItem
import io.github.wifi_password_manager.ui.shared.LoadingDialog
import io.github.wifi_password_manager.ui.shared.TooltipIconButton
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import io.github.wifi_password_manager.utils.isBiometricAuthenticationSupported
import io.github.wifi_password_manager.utils.plus
import io.github.wifi_password_manager.utils.toast

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingView(state: SettingViewModel.State, onAction: (SettingViewModel.Action) -> Unit) {
    val uriHandler = LocalUriHandler.current
    val navBackStack = LocalNavBackStack.current
    val context = LocalContext.current
    val resources = LocalResources.current

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    TooltipIconButton(
                        onClick = { navBackStack.removeLastOrNull() },
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        tooltip = stringResource(R.string.back),
                        positioning = TooltipAnchorPosition.Below,
                    )
                },
                title = { Text(text = stringResource(R.string.settings_title)) },
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding + PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Appearance Section
            item {
                SettingSection(title = stringResource(R.string.appearance_section)) {
                    LanguageItem(language = state.settings.language) {
                        onAction(SettingViewModel.Action.UpdateLanguage(it))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)

                    ThemeModeItem(themeMode = state.settings.themeMode) {
                        onAction(SettingViewModel.Action.UpdateThemeMode(it))
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)

                        ListItem(
                            modifier =
                                Modifier.clickable {
                                    onAction(
                                        SettingViewModel.Action.ToggleMaterialYou(
                                            !state.settings.useMaterialYou
                                        )
                                    )
                                },
                            headlineContent = {
                                Text(text = stringResource(R.string.material_you_title))
                            },
                            trailingContent = {
                                Switch(
                                    checked = state.settings.useMaterialYou,
                                    onCheckedChange = {
                                        onAction(SettingViewModel.Action.ToggleMaterialYou(it))
                                    },
                                )
                            },
                        )
                    }
                }
            }

            // Import & Export Section
            item {
                SettingSection(title = stringResource(R.string.import_export_section)) {
                    ListItem(
                        modifier =
                            Modifier.clickable { onAction(SettingViewModel.Action.ImportNetworks) },
                        headlineContent = { Text(text = stringResource(R.string.import_action)) },
                        supportingContent = {
                            Text(text = stringResource(R.string.import_description))
                        },
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)

                    ListItem(
                        modifier =
                            Modifier.clickable { onAction(SettingViewModel.Action.ExportNetworks) },
                        headlineContent = { Text(text = stringResource(R.string.export_action)) },
                        supportingContent = {
                            Text(text = stringResource(R.string.export_description))
                        },
                    )
                }
            }

            // Security Section
            item {
                SettingSection(title = stringResource(R.string.security_section)) {
                    val isAvailable = context.isBiometricAuthenticationSupported()
                    val appLockEnabled by rememberUpdatedState(state.settings.appLockEnabled)
                    val launcher = rememberAuthenticationLauncher { result ->
                        when (result) {
                            is AuthenticationResult.Error -> {
                                when (result.errorCode) {
                                    BiometricPrompt.ERROR_USER_CANCELED,
                                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                                    BiometricPrompt.ERROR_TIMEOUT,
                                    BiometricPrompt.ERROR_CANCELED -> Unit
                                    BiometricPrompt.ERROR_LOCKOUT ->
                                        context.toast(R.string.app_lock_too_many_attempts)
                                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT ->
                                        context.toast(R.string.app_lock_permanently_locked)
                                    else -> context.toast(R.string.app_lock_authentication_failed)
                                }
                            }
                            is AuthenticationResult.Success -> {
                                onAction(SettingViewModel.Action.ToggleAppLock(!appLockEnabled))
                            }
                        }
                    }
                    val handleToggle by rememberUpdatedState {
                        val request =
                            AuthenticationRequest.biometricRequest(
                                title =
                                    resources.getString(
                                        if (appLockEnabled) {
                                            R.string.app_lock_disable_title
                                        } else {
                                            R.string.app_lock_enable_title
                                        }
                                    ),
                                authFallback =
                                    AuthenticationRequest.Biometric.Fallback.DeviceCredential,
                            ) {}
                        launcher.launch(request)
                    }
                    ListItem(
                        modifier = Modifier.clickable(enabled = isAvailable) { handleToggle() },
                        headlineContent = { Text(text = stringResource(R.string.app_lock_title)) },
                        supportingContent = {
                            Text(text = stringResource(R.string.app_lock_description))
                        },
                        trailingContent = {
                            Switch(
                                checked = state.settings.appLockEnabled && isAvailable,
                                onCheckedChange = { handleToggle() },
                                enabled = isAvailable,
                            )
                        },
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)

                    ListItem(
                        modifier =
                            Modifier.clickable {
                                onAction(
                                    SettingViewModel.Action.ToggleSecureScreen(
                                        !state.settings.secureScreenEnabled
                                    )
                                )
                            },
                        headlineContent = {
                            Text(text = stringResource(R.string.secure_screen_title))
                        },
                        supportingContent = {
                            Text(text = stringResource(R.string.secure_screen_description))
                        },
                        trailingContent = {
                            Switch(
                                checked = state.settings.secureScreenEnabled,
                                onCheckedChange = {
                                    onAction(SettingViewModel.Action.ToggleSecureScreen(it))
                                },
                            )
                        },
                    )
                }
            }

            // Advanced Section
            item {
                SettingSection(title = stringResource(R.string.advanced_section)) {
                    ListItem(
                        modifier =
                            Modifier.clickable {
                                onAction(SettingViewModel.Action.ShowForgetAllDialog)
                            },
                        headlineContent = {
                            Text(text = stringResource(R.string.forget_all_action))
                        },
                        supportingContent = {
                            Text(text = stringResource(R.string.forget_all_description))
                        },
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)

                    ListItem(
                        modifier =
                            Modifier.clickable {
                                onAction(
                                    SettingViewModel.Action.ToggleAutoPersistEphemeralNetworks(
                                        !state.settings.autoPersistEphemeralNetworks
                                    )
                                )
                            },
                        headlineContent = {
                            Text(
                                text =
                                    stringResource(R.string.auto_persist_ephemeral_networks_action)
                            )
                        },
                        supportingContent = {
                            Text(
                                text =
                                    stringResource(
                                        R.string.auto_persist_ephemeral_networks_description
                                    )
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = state.settings.autoPersistEphemeralNetworks,
                                onCheckedChange = {
                                    onAction(
                                        SettingViewModel.Action.ToggleAutoPersistEphemeralNetworks(
                                            it
                                        )
                                    )
                                },
                            )
                        },
                    )
                }
            }

            // About Section
            item {
                SettingSection(title = stringResource(R.string.about_section)) {
                    ListItem(
                        modifier = Modifier.clickable { navBackStack.add(Route.LicenseScreen) },
                        headlineContent = { Text(text = stringResource(R.string.license_title)) },
                        supportingContent = {
                            Text(text = stringResource(R.string.license_description))
                        },
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)

                    val sourceCodeUrl = "https://github.com/Khh-vu/wifi-password-manager"
                    ListItem(
                        modifier = Modifier.clickable { uriHandler.openUri(sourceCodeUrl) },
                        headlineContent = {
                            Text(text = stringResource(R.string.source_code_title))
                        },
                        supportingContent = { Text(text = sourceCodeUrl) },
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)

                    ListItem(
                        headlineContent = { Text(text = stringResource(R.string.version_title)) },
                        supportingContent = { Text(text = BuildConfig.VERSION_NAME) },
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)

                    ListItem(
                        headlineContent = {
                            Text(text = stringResource(R.string.build_type_title))
                        },
                        supportingContent = { Text(text = BuildConfig.BUILD_TYPE) },
                    )
                }
            }
        }

        if (state.isLoading) {
            LoadingDialog()
        }

        if (state.showForgetAllDialog) {
            ForgetAllConfirmDialog(
                onDismiss = { onAction(SettingViewModel.Action.HideForgetAllDialog) },
                onConfirm = { onAction(SettingViewModel.Action.ConfirmForgetAllNetworks) },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SettingViewPreview() {
    WiFiPasswordManagerTheme { SettingView(state = SettingViewModel.State(), onAction = {}) }
}

@PreviewScreenSizes
@Composable
private fun AdaptiveSettingViewPreview() {
    WiFiPasswordManagerTheme { SettingView(state = SettingViewModel.State(), onAction = {}) }
}
