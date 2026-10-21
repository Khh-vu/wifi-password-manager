package io.github.wifi_password_manager.ui.screen.network.list.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.WifiConnectionStatus
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.navigation.Route
import io.github.wifi_password_manager.ui.icons.Close
import io.github.wifi_password_manager.ui.icons.Delete
import io.github.wifi_password_manager.ui.icons.EditNote
import io.github.wifi_password_manager.ui.icons.QrCode2
import io.github.wifi_password_manager.ui.icons.Wifi
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.theme.SurfaceWrapper
import io.github.wifi_password_manager.utils.MOCK

@Composable
fun WifiCardDropdownMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    network: WifiNetwork,
    connectionStatus: WifiConnectionStatus,
    isCacheMode: Boolean = false,
    onShowWifiQrRequest: () -> Unit,
    onForgetRequest: () -> Unit,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    val navBackStack = LocalNavBackStack.current
    val connected =
        connectionStatus is WifiConnectionStatus.Connected && connectionStatus.ssid == network.ssid

    DropdownMenuPopup(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShapes(shape = MenuDefaults.leadingGroupShape)
        ) {
            DropdownMenuItem(
                onClick = {},
                text = {
                    Text(text = network.ssid, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                enabled = false,
                colors =
                    MenuDefaults.itemColors(disabledTextColor = MaterialTheme.colorScheme.primary),
            )
        }

        Spacer(Modifier.height(MenuDefaults.GroupSpacing))

        DropdownMenuGroup(
            shapes = MenuDefaults.groupShapes(shape = MenuDefaults.middleGroupShape)
        ) {
            if (!isCacheMode) {
                DropdownMenuItem(
                    onClick = {
                        onDismissRequest()
                        onForgetRequest()
                    },
                    text = { Text(text = stringResource(R.string.forget_action)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Delete,
                            contentDescription = stringResource(R.string.forget_action),
                        )
                    },
                    colors =
                        MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.error,
                            leadingIconColor = MaterialTheme.colorScheme.error,
                        ),
                )

                DropdownMenuItem(
                    onClick = {
                        onDismissRequest()
                        if (connected) {
                            onAction(NetworkListViewModel.Action.Disconnect)
                        } else {
                            onAction(NetworkListViewModel.Action.Connect(network))
                        }
                    },
                    text = {
                        Text(
                            text =
                                stringResource(
                                    if (connected) R.string.disconnect_action
                                    else R.string.connect_action
                                )
                        )
                    },
                    enabled = connectionStatus !is WifiConnectionStatus.Connecting,
                    leadingIcon = {
                        Icon(
                            imageVector = if (connected) Close else Wifi,
                            contentDescription =
                                stringResource(
                                    if (connected) R.string.disconnect_action
                                    else R.string.connect_action
                                ),
                        )
                    },
                )
            }

            DropdownMenuItem(
                onClick = {
                    onDismissRequest()
                    onShowWifiQrRequest()
                },
                text = { Text(text = stringResource(R.string.wifi_qr_code)) },
                leadingIcon = {
                    Icon(
                        imageVector = QrCode2,
                        contentDescription = stringResource(R.string.show_wifi_qr_code),
                    )
                },
            )
        }

        Spacer(Modifier.height(MenuDefaults.GroupSpacing))

        DropdownMenuGroup(
            shapes = MenuDefaults.groupShapes(shape = MenuDefaults.trailingGroupShape)
        ) {
            DropdownMenuItem(
                onClick = {
                    onDismissRequest()
                    navBackStack.add(Route.NoteScreen(network = network))
                },
                text = {
                    Text(
                        text =
                            stringResource(
                                if (network.note != null) R.string.edit_note else R.string.add_note
                            )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = EditNote,
                        contentDescription =
                            stringResource(
                                if (network.note != null) R.string.edit_note else R.string.add_note
                            ),
                    )
                },
            )

            if (network.note != null) {
                DropdownMenuItem(
                    onClick = {
                        onDismissRequest()
                        onAction(NetworkListViewModel.Action.DeleteNote(network.ssid))
                    },
                    text = { Text(text = stringResource(R.string.delete_note)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Delete,
                            contentDescription = stringResource(R.string.delete_note),
                        )
                    },
                    colors =
                        MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.error,
                            leadingIconColor = MaterialTheme.colorScheme.error,
                        ),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun WifiCardDropdownMenuPreview() {
    WifiCardDropdownMenu(
        expanded = true,
        onDismissRequest = {},
        network = WifiNetwork.MOCK.random(),
        connectionStatus = WifiConnectionStatus.Disconnected,
        onShowWifiQrRequest = {},
        onForgetRequest = {},
        onAction = {},
    )
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun ConnectingWifiCardDropdownMenuPreview() {
    val network = WifiNetwork.MOCK.random()
    WifiCardDropdownMenu(
        expanded = true,
        onDismissRequest = {},
        network = network,
        connectionStatus = WifiConnectionStatus.Connecting(network.ssid),
        onShowWifiQrRequest = {},
        onForgetRequest = {},
        onAction = {},
    )
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun ConnectedWifiCardDropdownMenuPreview() {
    val network = WifiNetwork.MOCK.random()
    WifiCardDropdownMenu(
        expanded = true,
        onDismissRequest = {},
        network = network,
        connectionStatus = WifiConnectionStatus.Connected(network.ssid),
        onShowWifiQrRequest = {},
        onForgetRequest = {},
        onAction = {},
    )
}
