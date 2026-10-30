package io.github.wifi_password_manager.ui.screen.network.list

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.navigation.Route
import io.github.wifi_password_manager.ui.icons.ArrowUpward
import io.github.wifi_password_manager.ui.icons.Refresh
import io.github.wifi_password_manager.ui.screen.network.list.components.MethodSignatureErrorDialog
import io.github.wifi_password_manager.ui.screen.network.list.components.NetworkList
import io.github.wifi_password_manager.ui.screen.network.list.components.NetworkListTopBar
import io.github.wifi_password_manager.ui.theme.ThemeWrapper
import io.github.wifi_password_manager.utils.MOCK
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkListView(
    state: NetworkListViewModel.State,
    searchState: TextFieldState,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    val navBackStack = LocalNavBackStack.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val showScrollToTop by remember { derivedStateOf { gridState.firstVisibleItemIndex > 3 } }

    BackHandler(enabled = state.showingSearch) {
        onAction(NetworkListViewModel.Action.ToggleSearch)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            NetworkListTopBar(
                state = state,
                searchState = searchState,
                scrollBehavior = scrollBehavior,
                onAction = onAction,
            )
        },
        floatingActionButton = {
            Column(
                modifier = Modifier.imePadding(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AnimatedVisibility(
                    visible = showScrollToTop,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                ) {
                    TooltipBox(
                        positionProvider =
                            TooltipDefaults.rememberTooltipPositionProvider(
                                positioning = TooltipAnchorPosition.Above
                            ),
                        tooltip = {
                            PlainTooltip {
                                Text(text = stringResource(R.string.scroll_to_top_description))
                            }
                        },
                        state = rememberTooltipState(),
                    ) {
                        FloatingActionButton(
                            onClick = { scope.launch { gridState.animateScrollToItem(0) } }
                        ) {
                            Icon(
                                imageVector = ArrowUpward,
                                contentDescription =
                                    stringResource(R.string.scroll_to_top_description),
                            )
                        }
                    }
                }

                if (!state.isCacheMode) {
                    TooltipBox(
                        positionProvider =
                            TooltipDefaults.rememberTooltipPositionProvider(
                                positioning = TooltipAnchorPosition.Above
                            ),
                        tooltip = {
                            PlainTooltip {
                                Text(text = stringResource(R.string.refresh_description))
                            }
                        },
                        state = rememberTooltipState(),
                    ) {
                        FloatingActionButton(
                            onClick = { onAction(NetworkListViewModel.Action.Refresh) }
                        ) {
                            Icon(
                                imageVector = Refresh,
                                contentDescription = stringResource(R.string.refresh_description),
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().imePadding()

        if (state.savedNetworks.isEmpty()) {
            Box(modifier = modifier.padding(innerPadding), contentAlignment = Alignment.Center) {
                if (state.showingSearch) {
                    Text(
                        text = stringResource(R.string.no_networks_found),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                } else {
                    Text(
                        text = stringResource(R.string.no_networks_available),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }
            }
        } else {
            NetworkList(
                modifier = modifier,
                contentPadding = innerPadding,
                gridState = gridState,
                networks = state.savedNetworks,
                connectionStatus = state.connectionStatus,
                isCacheMode = state.isCacheMode,
                onAction = onAction,
            )
        }
    }

    if (state.showMethodSignatureError) {
        MethodSignatureErrorDialog(
            onDismiss = { onAction(NetworkListViewModel.Action.DismissMethodInspectorError) },
            onOpenMethodInspector = { navBackStack.add(Route.IWifiManagerMethodInspectorScreen) },
        )
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun NetworkListViewPreview() {
    NetworkListView(
        state = NetworkListViewModel.State(savedNetworks = WifiNetwork.MOCK),
        searchState = rememberTextFieldState(),
        onAction = {},
    )
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun EmptyNetworkListViewPreview() {
    NetworkListView(
        state = NetworkListViewModel.State(),
        searchState = rememberTextFieldState(),
        onAction = {},
    )
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun SearchNetworkListViewPreview() {
    NetworkListView(
        state =
            NetworkListViewModel.State(
                showingSearch = true,
                savedNetworks = WifiNetwork.MOCK,
            ),
        searchState = rememberTextFieldState(),
        onAction = {},
    )
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun EmptySearchNetworkListViewPreview() {
    NetworkListView(
        state = NetworkListViewModel.State(showingSearch = true),
        searchState = rememberTextFieldState(),
        onAction = {},
    )
}
