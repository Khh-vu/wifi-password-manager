package io.github.wifi_password_manager.ui.screen.network.list

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.data.WifiNetwork
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.navigation.SettingScreen
import io.github.wifi_password_manager.ui.screen.network.list.components.NetworkList
import io.github.wifi_password_manager.ui.screen.network.list.components.SearchBar
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import io.github.wifi_password_manager.utils.MOCK

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkListView(
    state: NetworkListViewModel.State,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    val navBackStack = LocalNavBackStack.current

    BackHandler(enabled = state.showingSearch) {
        onAction(NetworkListViewModel.Action.ToggleSearch)
    }

    Scaffold(
        topBar = {
            AnimatedContent(
                targetState = state.showingSearch,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) { showingSearch ->
                if (showingSearch) {
                    SearchBar(state = state, onAction = onAction)
                } else {
                    TopAppBar(
                        title = { Text(text = stringResource(R.string.network_list_title)) },
                        actions = {
                            TooltipBox(
                                positionProvider =
                                    TooltipDefaults.rememberTooltipPositionProvider(
                                        positioning = TooltipAnchorPosition.Below
                                    ),
                                tooltip = {
                                    PlainTooltip {
                                        Text(text = stringResource(R.string.search_tooltip))
                                    }
                                },
                                state = rememberTooltipState(),
                            ) {
                                IconButton(
                                    onClick = { onAction(NetworkListViewModel.Action.ToggleSearch) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = stringResource(R.string.search_tooltip),
                                    )
                                }
                            }

                            TooltipBox(
                                positionProvider =
                                    TooltipDefaults.rememberTooltipPositionProvider(
                                        positioning = TooltipAnchorPosition.Below
                                    ),
                                tooltip = {
                                    PlainTooltip {
                                        Text(text = stringResource(R.string.settings_tooltip))
                                    }
                                },
                                state = rememberTooltipState(),
                            ) {
                                IconButton(onClick = { navBackStack.add(SettingScreen) }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription =
                                            stringResource(R.string.settings_tooltip),
                                    )
                                }
                            }
                        },
                    )
                }
            }
        },
        floatingActionButton = {
            TooltipBox(
                positionProvider =
                    TooltipDefaults.rememberTooltipPositionProvider(
                        positioning = TooltipAnchorPosition.Above
                    ),
                tooltip = {
                    PlainTooltip { Text(text = stringResource(R.string.refresh_description)) }
                },
                state = rememberTooltipState(),
            ) {
                FloatingActionButton(
                    modifier = Modifier.navigationBarsPadding().imePadding(),
                    onClick = { onAction(NetworkListViewModel.Action.Refresh) },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.refresh_description),
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                networks = state.savedNetworks,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun NetworkListViewPreview() {
    WiFiPasswordManagerTheme {
        NetworkListView(
            state = NetworkListViewModel.State(savedNetworks = WifiNetwork.MOCK),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun EmptyNetworkListViewPreview() {
    WiFiPasswordManagerTheme {
        NetworkListView(state = NetworkListViewModel.State(), onAction = {})
    }
}

@PreviewLightDark
@Composable
private fun SearchNetworkListViewPreview() {
    WiFiPasswordManagerTheme {
        NetworkListView(
            state =
                NetworkListViewModel.State(showingSearch = true, savedNetworks = WifiNetwork.MOCK),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun EmptySearchNetworkListViewPreview() {
    WiFiPasswordManagerTheme {
        NetworkListView(state = NetworkListViewModel.State(showingSearch = true), onAction = {})
    }
}
