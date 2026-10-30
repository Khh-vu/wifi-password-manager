package io.github.wifi_password_manager.ui.screen.network.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.composeunstyled.Thumb
import com.composeunstyled.UnstyledVerticalScrollbar
import com.composeunstyled.rememberScrollbarState
import io.github.wifi_password_manager.domain.model.WifiConnectionStatus
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.theme.SurfaceWrapper
import io.github.wifi_password_manager.utils.DeviceConfiguration
import io.github.wifi_password_manager.utils.MOCK
import io.github.wifi_password_manager.utils.plus

@Composable
fun NetworkList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    gridState: LazyGridState = rememberLazyGridState(),
    networks: List<WifiNetwork>,
    connectionStatus: WifiConnectionStatus,
    isCacheMode: Boolean = false,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
    val isMobile = deviceConfiguration == DeviceConfiguration.MOBILE_PORTRAIT
    val spacing = if (isMobile) 12.dp else 16.dp

    val scrollbarState = rememberScrollbarState(gridState)

    Box(modifier = modifier) {
        LazyVerticalGrid(
            columns =
                when (deviceConfiguration) {
                    DeviceConfiguration.MOBILE_PORTRAIT -> GridCells.Fixed(1)
                    DeviceConfiguration.TABLET_PORTRAIT -> GridCells.Fixed(2)
                    else -> GridCells.Adaptive(400.dp)
                },
            state = gridState,
            contentPadding = contentPadding + PaddingValues(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            items(items = networks, key = { it.ssid }, contentType = { it.password.isEmpty() }) {
                network ->
                WifiCard(
                    network = network,
                    connectionStatus = connectionStatus,
                    isCacheMode = isCacheMode,
                    expanded = !isMobile,
                    onAction = onAction,
                )
            }
        }

        UnstyledVerticalScrollbar(
            scrollbarState = scrollbarState,
            modifier =
                Modifier.align(Alignment.TopEnd)
                    .fillMaxHeight()
                    .width(8.dp)
                    .padding(
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding(),
                    )
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .2f), CircleShape),
        ) {
            Thumb(
                modifier =
                    Modifier.fillMaxWidth()
                        .heightIn(min = 36.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = .9f),
                            CircleShape,
                        )
            )
        }
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun NetworkListPreview() {
    NetworkList(
        networks = WifiNetwork.MOCK,
        connectionStatus = WifiConnectionStatus.Disconnected,
        onAction = {},
    )
}

@PreviewScreenSizes
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun AdaptiveNetworkListPreview() {
    NetworkList(
        networks = WifiNetwork.MOCK,
        connectionStatus = WifiConnectionStatus.Disconnected,
        onAction = {},
    )
}
