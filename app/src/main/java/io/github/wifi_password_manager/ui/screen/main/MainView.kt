package io.github.wifi_password_manager.ui.screen.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.data.WifiNetwork
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.navigation.SettingScreen
import io.github.wifi_password_manager.ui.shared.WifiCard
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainView(state: MainViewModel.State, onEvent: (MainViewModel.Event) -> Unit) {
    val navBackStack = LocalNavBackStack.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Saved WiFi Networks") },
                actions = {
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
        },
        floatingActionButton = {
            var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

            BackHandler(enabled = fabMenuExpanded) { fabMenuExpanded = false }

            FloatingActionButtonMenu(
                expanded = fabMenuExpanded,
                button = {
                    ToggleFloatingActionButton(
                        checked = fabMenuExpanded,
                        onCheckedChange = { fabMenuExpanded = it },
                    ) {
                        val imageVector by remember {
                            derivedStateOf {
                                if (checkedProgress > 0.5f) Icons.Filled.Close
                                else Icons.Filled.Menu
                            }
                        }

                        Icon(
                            imageVector = imageVector,
                            contentDescription = "More options",
                            modifier = Modifier.animateIcon(checkedProgress = { checkedProgress }),
                        )
                    }
                },
            ) {
                FloatingActionButtonMenuItem(
                    icon = {
                        Icon(imageVector = Icons.Filled.FileDownload, contentDescription = "Import")
                    },
                    text = { Text(text = "Import") },
                    onClick = {
                        // TODO: Implement import functionality
                        fabMenuExpanded = false
                    },
                )

                FloatingActionButtonMenuItem(
                    icon = {
                        Icon(imageVector = Icons.Filled.FileUpload, contentDescription = "Export")
                    },
                    text = { Text(text = "Export") },
                    onClick = {
                        // TODO: Implement export functionality
                        fabMenuExpanded = false
                    },
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

@Preview
@Composable
private fun MainViewPreview() {
    WiFiPasswordManagerTheme {
        MainView(state = MainViewModel.State(savedNetworks = WifiNetwork.MOCK), onEvent = {})
    }
}
