package io.github.wifi_password_manager.ui.screen.setting.components

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.PrivilegedMode
import io.github.wifi_password_manager.ui.UiConfig
import io.github.wifi_password_manager.ui.theme.ThemeWrapper

private enum class ConnectedWifiInfoStatus {
    ACTIVE,
    PERMISSION_REQUIRED,
    LOCATION_DISABLED,
}

@Composable
fun ConnectedWifiInfoItem(modifier: Modifier = Modifier, mode: PrivilegedMode) {
    val context = LocalContext.current
    val isInspectMode = LocalInspectionMode.current

    val locationManager =
        remember(context) {
            if (isInspectMode) {
                null
            } else {
                context.getSystemService<LocationManager>()
            }
        }
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var locationEnabled by remember { mutableStateOf(locationManager?.isLocationEnabled == true) }
    val status by
        remember(mode, permissionGranted, locationEnabled) {
            derivedStateOf {
                when {
                    mode.hasPrivilegedAccess -> ConnectedWifiInfoStatus.ACTIVE
                    !permissionGranted -> ConnectedWifiInfoStatus.PERMISSION_REQUIRED
                    !locationEnabled -> ConnectedWifiInfoStatus.LOCATION_DISABLED
                    else -> ConnectedWifiInfoStatus.ACTIVE
                }
            }
        }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            permissionGranted = it
        }

    DisposableEffect(Unit) {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    locationEnabled =
                        intent.getBooleanExtra(LocationManager.EXTRA_LOCATION_ENABLED, false)
                }
            }
        context.registerReceiver(receiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
        onDispose { context.unregisterReceiver(receiver) }
    }

    ListItem(
        modifier = modifier,
        supportingContent = {
            Text(
                text =
                    when (status) {
                        ConnectedWifiInfoStatus.ACTIVE -> {
                            if (mode.hasPrivilegedAccess) {
                                stringResource(
                                    R.string.connected_wifi_active_with_access,
                                    mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                )
                            } else {
                                stringResource(R.string.connected_wifi_active)
                            }
                        }

                        ConnectedWifiInfoStatus.PERMISSION_REQUIRED ->
                            stringResource(R.string.location_permission_required)
                        ConnectedWifiInfoStatus.LOCATION_DISABLED ->
                            stringResource(R.string.location_services_disabled)
                    },
                color =
                    when (status) {
                        ConnectedWifiInfoStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.error
                    },
                style =
                    if (status == ConnectedWifiInfoStatus.ACTIVE) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        LocalTextStyle.current
                    },
            )
        },
        trailingContent = {
            when (status) {
                ConnectedWifiInfoStatus.PERMISSION_REQUIRED -> {
                    TextButton(
                        onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(text = stringResource(R.string.grant))
                    }
                }

                ConnectedWifiInfoStatus.LOCATION_DISABLED -> {
                    TextButton(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        },
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(text = stringResource(R.string.open_settings))
                    }
                }

                else -> Unit
            }
        },
        shapes = UiConfig.listItemShapes(),
        colors = UiConfig.listItemColors(),
    ) {
        Text(text = stringResource(R.string.show_connected_wifi))
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun ConnectedWifiInfoItemPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PrivilegedMode.entries.forEach { ConnectedWifiInfoItem(mode = it) }
    }
}
