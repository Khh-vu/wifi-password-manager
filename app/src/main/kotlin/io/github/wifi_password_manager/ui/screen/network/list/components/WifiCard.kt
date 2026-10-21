package io.github.wifi_password_manager.ui.screen.network.list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.LocalSettings
import io.github.wifi_password_manager.domain.model.WifiConnectionStatus
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.navigation.Route
import io.github.wifi_password_manager.ui.icons.ContentCopy
import io.github.wifi_password_manager.ui.icons.MoreVert
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.shared.TooltipIconButton
import io.github.wifi_password_manager.ui.shared.WarningConfirmDialog
import io.github.wifi_password_manager.ui.theme.SurfaceWrapper
import io.github.wifi_password_manager.utils.MOCK
import io.github.wifi_password_manager.utils.getSecurity
import io.github.wifi_password_manager.utils.passwordClipEntry
import kotlinx.coroutines.launch

private sealed interface OptionState {
    data object WifiQR : OptionState

    data object ForgetConfirm : OptionState
}

@Composable
fun WifiCard(
    modifier: Modifier = Modifier,
    network: WifiNetwork,
    connectionStatus: WifiConnectionStatus,
    isCacheMode: Boolean = false,
    expanded: Boolean = false,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    val navBackStack = LocalNavBackStack.current
    var optionState by remember { mutableStateOf<OptionState?>(null) }
    val connected =
        connectionStatus is WifiConnectionStatus.Connected && connectionStatus.ssid == network.ssid

    Card(
        modifier = modifier,
        colors =
            if (connected) {
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            } else {
                CardDefaults.cardColors()
            },
    ) {
        SSIDItem(
            network = network,
            connectionStatus = connectionStatus,
            isCacheMode = isCacheMode,
            onOptionStateChange = { optionState = it },
            onAction = onAction,
        )

        if (network.password.isNotEmpty() || expanded) {
            Separator(connected = connected)
            PasswordItem(network = network)
        }

        if (network.note != null) {
            Separator(connected = connected)
            NoteItem(
                modifier =
                    Modifier.clickable { navBackStack.add(Route.NoteScreen(network = network)) },
                network = network,
            )
        }
    }

    when (optionState) {
        OptionState.WifiQR -> {
            WifiQRDialog(network = network, onDismiss = { optionState = null })
        }

        OptionState.ForgetConfirm -> {
            WarningConfirmDialog(
                title = stringResource(R.string.forget_confirmation_title),
                message = stringResource(R.string.forget_confirmation_message, network.ssid),
                onDismiss = { optionState = null },
                onConfirm = {
                    optionState = null
                    onAction(NetworkListViewModel.Action.Forget(network))
                },
            )
        }

        null -> Unit
    }
}

@Composable
private fun Separator(modifier: Modifier = Modifier, connected: Boolean = false) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = 16.dp),
        color =
            if (connected) MaterialTheme.colorScheme.onPrimaryContainer
            else ListItemDefaults.colors().supportingContentColor,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SSIDItem(
    modifier: Modifier = Modifier,
    network: WifiNetwork,
    connectionStatus: WifiConnectionStatus,
    isCacheMode: Boolean = false,
    onOptionStateChange: (OptionState?) -> Unit,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }

    ListItem(
        modifier = modifier,
        supportingContent = {
            Text(
                text =
                    when (connectionStatus) {
                        is WifiConnectionStatus.Connecting if
                            connectionStatus.ssid == network.ssid
                         -> {
                            stringResource(R.string.connecting_status)
                        }

                        else -> network.getSecurity(context)
                    }
            )
        },
        trailingContent = {
            TooltipIconButton(
                onClick = { expanded = true },
                imageVector = MoreVert,
                tooltip = stringResource(R.string.more_options),
                positioning = TooltipAnchorPosition.Below,
            )

            WifiCardDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                network = network,
                connectionStatus = connectionStatus,
                isCacheMode = isCacheMode,
                onShowWifiQrRequest = { onOptionStateChange(OptionState.WifiQR) },
                onForgetRequest = { onOptionStateChange(OptionState.ForgetConfirm) },
                onAction = onAction,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    ) {
        Text(
            text = network.ssid,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PasswordItem(modifier: Modifier = Modifier, network: WifiNetwork) {
    val plaintextPasswords = LocalSettings.current.plaintextPasswords
    var obscured by retain { mutableStateOf(!plaintextPasswords) }

    val trailingContent =
        @Composable {
            val clipboard = LocalClipboard.current
            val scope = rememberCoroutineScope()

            Button(
                onClick = {
                    scope.launch { clipboard.setClipEntry(network.passwordClipEntry(obscured)) }
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Icon(
                    imageVector = ContentCopy,
                    contentDescription = stringResource(R.string.copy_description),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.copy_action))
            }
        }

    ListItem(
        modifier = modifier,
        supportingContent = {
            if (network.password.isNotEmpty()) {
                Text(
                    text =
                        if (obscured) {
                            stringResource(R.string.password_mask_character)
                                .repeat(network.password.length)
                        } else {
                            network.password
                        },
                    modifier =
                        Modifier.clickable(enabled = !plaintextPasswords) { obscured = !obscured },
                    letterSpacing = if (obscured) 2.sp else TextUnit.Unspecified,
                )
            } else {
                Text(text = stringResource(R.string.no_password))
            }
        },
        trailingContent = trailingContent.takeIf { network.password.isNotEmpty() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    ) {
        Text(
            text = stringResource(R.string.password_label),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun NoteItem(modifier: Modifier = Modifier, network: WifiNetwork) {
    ListItem(
        modifier = modifier,
        supportingContent = {
            Text(text = network.note.orEmpty(), maxLines = 3, overflow = TextOverflow.Ellipsis)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    ) {
        Text(
            text = stringResource(R.string.note_label),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun WifiCardPreview() {
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(WifiNetwork.MOCK) { index, network ->
            WifiCard(
                network = network,
                connectionStatus =
                    if (index == 0) {
                        WifiConnectionStatus.Connected(network.ssid)
                    } else {
                        WifiConnectionStatus.Disconnected
                    },
                onAction = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun ConnectingWifiCardPreview() {
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(WifiNetwork.MOCK) { index, network ->
            WifiCard(
                network = network,
                connectionStatus =
                    if (index == 0) {
                        WifiConnectionStatus.Connecting(network.ssid)
                    } else {
                        WifiConnectionStatus.Disconnected
                    },
                onAction = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun ExpandedWifiCardPreview() {
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(WifiNetwork.MOCK) { index, network ->
            WifiCard(
                network = network,
                connectionStatus =
                    if (index == 0) {
                        WifiConnectionStatus.Connected(network.ssid)
                    } else {
                        WifiConnectionStatus.Disconnected
                    },
                expanded = true,
                onAction = {},
            )
        }
    }
}
