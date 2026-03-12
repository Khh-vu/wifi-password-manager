package io.github.wifi_password_manager.ui.screen.network.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.navigation.Route
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.shared.TooltipIconButton
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import io.github.wifi_password_manager.utils.MOCK
import io.github.wifi_password_manager.utils.getSecurity
import io.github.wifi_password_manager.utils.passwordClipEntry
import kotlinx.coroutines.launch

private sealed interface OptionState {
    data object WifiQR : OptionState
}

@Composable
fun WifiCard(
    modifier: Modifier = Modifier,
    network: WifiNetwork,
    expanded: Boolean = false,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    val navBackStack = LocalNavBackStack.current
    var optionState by remember { mutableStateOf<OptionState?>(null) }

    ElevatedCard(modifier = modifier) {
        SSIDItem(network = network, onOptionStateChange = { optionState = it }, onAction = onAction)

        if (network.password.isNotEmpty() || expanded) {
            HorizontalDivider(
                modifier =
                    Modifier.background(color = ListItemDefaults.containerColor)
                        .padding(horizontal = 16.dp)
            )

            PasswordItem(network = network)
        }

        if (network.note != null) {
            HorizontalDivider(
                modifier =
                    Modifier.background(color = ListItemDefaults.containerColor)
                        .padding(horizontal = 16.dp)
            )

            NoteItem(
                modifier =
                    Modifier.clickable { navBackStack.add(Route.NoteScreen(network = network)) },
                network = network,
            )
        }
    }

    when (optionState) {
        OptionState.WifiQR -> WifiQRDialog(network = network, onDismiss = { optionState = null })
        null -> Unit
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SSIDItem(
    modifier: Modifier = Modifier,
    network: WifiNetwork,
    onOptionStateChange: (OptionState?) -> Unit,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    val navBackStack = LocalNavBackStack.current
    val context = LocalContext.current

    var expanded by retain { mutableStateOf(false) }

    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(
                text = network.ssid,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = { Text(text = network.getSecurity(context)) },
        trailingContent = {
            TooltipIconButton(
                onClick = { expanded = true },
                painter = painterResource(R.drawable.ic_more_vert),
                tooltip = stringResource(R.string.more_options),
                positioning = TooltipAnchorPosition.Below,
            )

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    onClick = {},
                    text = {
                        Text(text = network.ssid, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    shape = MenuDefaults.standaloneItemShape,
                    enabled = false,
                    colors =
                        MenuDefaults.itemColors(
                            disabledTextColor = MaterialTheme.colorScheme.primary
                        ),
                )

                HorizontalDivider(
                    modifier = Modifier.padding(MenuDefaults.HorizontalDividerPadding)
                )

                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onOptionStateChange(OptionState.WifiQR)
                    },
                    text = { Text(text = stringResource(R.string.wifi_qr_code)) },
                    shape = MenuDefaults.standaloneItemShape,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_qr_code_2),
                            contentDescription = stringResource(R.string.show_wifi_qr_code),
                        )
                    },
                )

                HorizontalDivider(
                    modifier = Modifier.padding(MenuDefaults.HorizontalDividerPadding)
                )

                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        navBackStack.add(Route.NoteScreen(network = network))
                    },
                    shape = MenuDefaults.standaloneItemShape,
                    text = {
                        Text(
                            text =
                                stringResource(
                                    if (network.note != null) R.string.edit_note
                                    else R.string.add_note
                                )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit_note),
                            contentDescription =
                                stringResource(
                                    if (network.note != null) R.string.edit_note
                                    else R.string.add_note
                                ),
                        )
                    },
                )

                if (network.note != null) {
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onAction(NetworkListViewModel.Action.DeleteNote(network.ssid))
                        },
                        text = { Text(text = stringResource(R.string.delete_note)) },
                        shape = MenuDefaults.standaloneItemShape,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete),
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
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PasswordItem(modifier: Modifier = Modifier, network: WifiNetwork) {
    var obscured by retain { mutableStateOf(true) }

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
                    painter = painterResource(R.drawable.ic_content_copy),
                    contentDescription = stringResource(R.string.copy_description),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.copy_action))
            }
        }

    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(
                text = stringResource(R.string.password_label),
                style = MaterialTheme.typography.titleMedium,
            )
        },
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
                    modifier = Modifier.clickable { obscured = !obscured },
                    letterSpacing = if (obscured) 2.sp else TextUnit.Unspecified,
                )
            } else {
                Text(text = stringResource(R.string.no_password))
            }
        },
        trailingContent = trailingContent.takeIf { network.password.isNotEmpty() },
    )
}

@Composable
private fun NoteItem(modifier: Modifier = Modifier, network: WifiNetwork) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(
                text = stringResource(R.string.note_label),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            Text(text = network.note.orEmpty(), maxLines = 3, overflow = TextOverflow.Ellipsis)
        },
    )
}

@PreviewLightDark
@Composable
private fun WifiCardPreview() {
    WiFiPasswordManagerTheme {
        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(WifiNetwork.MOCK) { WifiCard(network = it, onAction = {}) }
        }
    }
}

@PreviewLightDark
@Composable
private fun ExpandedWifiCardPreview() {
    WiFiPasswordManagerTheme {
        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(WifiNetwork.MOCK) { WifiCard(network = it, expanded = true, onAction = {}) }
        }
    }
}
