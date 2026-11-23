package io.github.wifi_password_manager.ui.screen.network.list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorPosition
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.navigation.Route
import io.github.wifi_password_manager.ui.icons.Add
import io.github.wifi_password_manager.ui.icons.Delete
import io.github.wifi_password_manager.ui.icons.Download
import io.github.wifi_password_manager.ui.icons.MoreVert
import io.github.wifi_password_manager.ui.icons.QrCodeScanner
import io.github.wifi_password_manager.ui.icons.Refresh
import io.github.wifi_password_manager.ui.icons.Search
import io.github.wifi_password_manager.ui.icons.Upload
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.shared.TooltipIconButton
import io.github.wifi_password_manager.ui.theme.ThemeWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkListToolbar(
    modifier: Modifier = Modifier,
    state: NetworkListViewModel.State,
    onAction: (NetworkListViewModel.Action) -> Unit,
    onScanQr: () -> Unit,
) {
    val navBackStack = LocalNavBackStack.current

    HorizontalFloatingToolbar(
        expanded = true,
        floatingActionButton = {
            TooltipBox(
                positionProvider =
                    TooltipDefaults.rememberTooltipPositionProvider(
                        positioning = TooltipAnchorPosition.Above,
                    ),
                tooltip = { PlainTooltip { Text(text = stringResource(R.string.add_network)) } },
                state = rememberTooltipState(),
            ) {
                FloatingToolbarDefaults.StandardFloatingActionButton(
                    onClick = { navBackStack.add(Route.AddNetworkScreen) },
                ) {
                    Icon(
                        imageVector = Add,
                        contentDescription = stringResource(R.string.add_network),
                    )
                }
            }
        },
        modifier = modifier,
    ) {
        TooltipIconButton(
            onClick = { onAction(NetworkListViewModel.Action.ToggleSearch) },
            imageVector = Search,
            tooltip = stringResource(R.string.search_tooltip),
            positioning = TooltipAnchorPosition.Above,
        )

        SortFilterButton(
            filters = state.filters,
            sortBy = state.sortBy,
            sortAscending = state.sortAscending,
            onAction = onAction,
            positioning = TooltipAnchorPosition.Above,
        )

        if (!state.isCacheMode) {
            TooltipIconButton(
                onClick = { onAction(NetworkListViewModel.Action.Refresh) },
                imageVector = Refresh,
                tooltip = stringResource(R.string.refresh_description),
                positioning = TooltipAnchorPosition.Above,
            )
            TooltipIconButton(
                onClick = onScanQr,
                imageVector = QrCodeScanner,
                tooltip = stringResource(R.string.scan_wifi_qr),
                positioning = TooltipAnchorPosition.Above,
            )
        }

        MoreMenuButton(isCacheMode = state.isCacheMode, onAction = onAction)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreMenuButton(isCacheMode: Boolean, onAction: (NetworkListViewModel.Action) -> Unit) {
    var expanded by retain { mutableStateOf(false) }

    Box {
        TooltipIconButton(
            onClick = { expanded = true },
            imageVector = MoreVert,
            tooltip = stringResource(R.string.more_options),
            positioning = TooltipAnchorPosition.Above,
        )

        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            popupPositionProvider =
                MenuDefaults.rememberDropdownMenuPopupPositionProvider(MenuAnchorPosition.Above),
        ) {
            DropdownMenuGroup(
                shapes =
                    MenuDefaults.groupShapes(
                        shape =
                            if (isCacheMode) MenuDefaults.standaloneGroupShape
                            else MenuDefaults.leadingGroupShape,
                    ),
            ) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onAction(NetworkListViewModel.Action.ImportNetworks)
                    },
                    text = { Text(text = stringResource(R.string.import_action)) },
                    leadingIcon = { Icon(imageVector = Download, contentDescription = null) },
                )

                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onAction(NetworkListViewModel.Action.ShowExportDialog)
                    },
                    text = { Text(text = stringResource(R.string.export_action)) },
                    leadingIcon = { Icon(imageVector = Upload, contentDescription = null) },
                )
            }

            if (!isCacheMode) {
                Spacer(Modifier.height(MenuDefaults.GroupSpacing))

                DropdownMenuGroup(
                    shapes = MenuDefaults.groupShapes(shape = MenuDefaults.trailingGroupShape),
                ) {
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onAction(NetworkListViewModel.Action.ShowForgetAllDialog)
                        },
                        text = { Text(text = stringResource(R.string.forget_all_title)) },
                        leadingIcon = { Icon(imageVector = Delete, contentDescription = null) },
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
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun BottomNetworkListToolbarPreview() {
    NetworkListToolbar(
        state = NetworkListViewModel.State(),
        onAction = {},
        onScanQr = {},
    )
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun CacheModeNetworkListToolbarPreview() {
    NetworkListToolbar(
        state = NetworkListViewModel.State(isCacheMode = true),
        onAction = {},
        onScanQr = {},
    )
}
