package io.github.wifi_password_manager.ui.screen.network.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.data.WifiNetwork
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import io.github.wifi_password_manager.utils.DeviceConfiguration
import io.github.wifi_password_manager.utils.MOCK
import io.github.wifi_password_manager.utils.plus
import kotlinx.collections.immutable.ImmutableList

@Composable
fun NetworkList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    networks: ImmutableList<WifiNetwork>,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

    when (deviceConfiguration) {
        DeviceConfiguration.MOBILE_PORTRAIT -> {
            LazyColumn(
                modifier = modifier,
                contentPadding = contentPadding + PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = networks,
                    key = { it.ssid },
                    contentType = { it.password.isEmpty() },
                ) { network ->
                    WifiCard(network = network)
                }
            }
        }
        else -> {
            LazyVerticalGrid(
                modifier = modifier,
                columns =
                    if (deviceConfiguration == DeviceConfiguration.TABLET_PORTRAIT)
                        GridCells.Fixed(2)
                    else GridCells.Adaptive(400.dp),
                contentPadding = contentPadding + PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(
                    items = networks,
                    key = { it.ssid },
                    contentType = { it.password.isEmpty() },
                ) { network ->
                    WifiCard(network = network, expanded = true)
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun NetworkListPreview() {
    WiFiPasswordManagerTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            NetworkList(networks = WifiNetwork.MOCK)
        }
    }
}

@PreviewScreenSizes
@Composable
private fun AdaptiveNetworkListPreview() {
    WiFiPasswordManagerTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            NetworkList(networks = WifiNetwork.MOCK)
        }
    }
}
