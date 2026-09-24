package io.github.wifi_password_manager.ui.screen.network.list

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.navigation.Route
import io.github.wifi_password_manager.ui.icons.Refresh
import io.github.wifi_password_manager.ui.icons.Search
import io.github.wifi_password_manager.ui.icons.Settings
import io.github.wifi_password_manager.ui.screen.network.list.components.MethodSignatureErrorDialog
import io.github.wifi_password_manager.ui.screen.network.list.components.NetworkList
import io.github.wifi_password_manager.ui.shared.SearchBar
import io.github.wifi_password_manager.ui.shared.TooltipIconButton
import io.github.wifi_password_manager.ui.theme.ThemeWrapper
import io.github.wifi_password_manager.utils.MOCK

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkListView(
    state: NetworkListViewModel.State,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    val navBackStack = LocalNavBackStack.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    BackHandler(enabled = state.showingSearch) { onAction(NetworkListViewModel.Action.ToggleSearch) }

    Scaffold(
        topBar = {
            AnimatedContent(
                targetState = state.showingSearch,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) { showingSearch ->
                if (showingSearch) {
                    SearchBar(
                        showingSearch = state.showingSearch,
                        searchText = state.searchText,
                        onSearchTextChanged = {
                            onAction(NetworkListViewModel.Action.SearchTextChanged(it))
                        },
                        onBack = { onAction(NetworkListViewModel.Action.ToggleSearch) },
                        placeholder = stringResource(R.string.search_hint),
                    )
                } else {
                    TopAppBar(
                        title = {
                            if (state.savedNetworks.isNotEmpty()) {
                                Text(
                                    text = pluralStringResource(
                                        R.plurals.network_list_title,
                                        state.savedNetworks.size,
                                        state.savedNetworks.size,
                                    )
                                )
                            }
                        },
                        actions = {
                            TooltipIconButton(
                                onClick = { onAction(NetworkListViewModel.Action.ToggleSearch) },
                                imageVector = Search,
                                tooltip = stringResource(R.string.search_tooltip),
                                positioning = TooltipAnchorPosition.Below,
                            )

                            TooltipIconButton(
                                onClick = { navBackStack.add(Route.SettingScreen) },
                                imageVector = Settings,
                                tooltip = stringResource(R.string.settings_tooltip),
                                positioning = TooltipAnchorPosition.Below,
                            )
                        },
                        scrollBehavior = scrollBehavior,
                    )
                }
            }
        },
        floatingActionButton = {
            if (!state.isCacheMode) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.Above),
                    tooltip = { PlainTooltip { Text(text = stringResource(R.string.refresh_description)) } },
                    state = rememberTooltipState(),
                ) {
                    FloatingActionButton(
                        modifier = Modifier.imePadding(),
                        onClick = { onAction(NetworkListViewModel.Action.Refresh) },
                    ) {
                        Icon(
                            imageVector = Refresh,
                            contentDescription = stringResource(R.string.refresh_description),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        val modifier =
            Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .imePadding()

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
        onAction = {},
    )
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun EmptyNetworkListViewPreview() {
    NetworkListView(state = NetworkListViewModel.State(), onAction = {})
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun SearchNetworkListViewPreview() {
    NetworkListView(
        state = NetworkListViewModel.State(
            showingSearch = true, savedNetworks = WifiNetwork.MOCK
        ),
        onAction = {},
    )
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun EmptySearchNetworkListViewPreview() {
    NetworkListView(state = NetworkListViewModel.State(showingSearch = true), onAction = {})
}
