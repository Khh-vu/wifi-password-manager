package io.github.wifi_password_manager.ui.screen.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.data.WifiNetwork
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.navigation.SettingScreen
import io.github.wifi_password_manager.ui.screen.main.components.MainFloatingActionButtonMenu
import io.github.wifi_password_manager.ui.screen.main.components.SearchBar
import io.github.wifi_password_manager.ui.screen.main.components.WifiCard
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(state: MainViewModel.State, onEvent: (MainViewModel.Event) -> Unit) {
    val navBackStack = LocalNavBackStack.current

    BackHandler(enabled = state.showingSearch) { onEvent(MainViewModel.Event.ToggleSearch) }

    Scaffold(
        topBar = {
            AnimatedVisibility(visible = state.showingSearch, enter = fadeIn(), exit = fadeOut()) {
                SearchBar(state = state, onEvent = onEvent)
            }

            AnimatedVisibility(visible = !state.showingSearch, enter = fadeIn(), exit = fadeOut()) {
                TopAppBar(
                    title = { Text(text = "Saved WiFi Networks") },
                    actions = {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                            tooltip = { PlainTooltip { Text(text = "Search") } },
                            state = rememberTooltipState(),
                        ) {
                            IconButton(onClick = { onEvent(MainViewModel.Event.ToggleSearch) }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search",
                                )
                            }
                        }

                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                            tooltip = { PlainTooltip { Text(text = "Settings") } },
                            state = rememberTooltipState(),
                        ) {
                            IconButton(onClick = { navBackStack.add(SettingScreen) }) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Settings",
                                )
                            }
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (!state.showingSearch) {
                MainFloatingActionButtonMenu(
                    onImportClick = {
                        // TODO: Implement import functionality
                    },
                    onExportClick = { onEvent(MainViewModel.Event.ExportNetworks) },
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier.padding(innerPadding),
            isRefreshing = state.isLoading,
            onRefresh = { onEvent(MainViewModel.Event.GetSavedNetworks) },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = state.savedNetworks, key = { item -> item.key }) { network ->
                    WifiCard(network = network)
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MainViewPreview() {
    WiFiPasswordManagerTheme {
        MainView(state = MainViewModel.State(savedNetworks = WifiNetwork.MOCK), onEvent = {})
    }
}

@PreviewLightDark
@Composable
private fun SearchMainViewPreview() {
    WiFiPasswordManagerTheme {
        MainView(state = MainViewModel.State(showingSearch = true), onEvent = {})
    }
}
