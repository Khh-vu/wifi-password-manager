package io.github.wifi_password_manager.ui.screen.network.list.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.sp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.LocalSettings
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.navigation.LocalNavBackStack
import io.github.wifi_password_manager.navigation.Route
import io.github.wifi_password_manager.ui.icons.Search
import io.github.wifi_password_manager.ui.icons.Settings
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.shared.SearchBar
import io.github.wifi_password_manager.ui.shared.TooltipIconButton
import io.github.wifi_password_manager.ui.theme.ThemeWrapper
import io.github.wifi_password_manager.utils.MOCK

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkListTopBar(
    state: NetworkListViewModel.State,
    searchState: TextFieldState,
    scrollBehavior: TopAppBarScrollBehavior,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    val navBackStack = LocalNavBackStack.current
    val searchAtBottom = LocalSettings.current.searchBarAtBottom

    val topBarColors = TopAppBarDefaults.topAppBarColors()

    Surface(
        color =
            lerp(
                topBarColors.containerColor,
                topBarColors.scrolledContainerColor,
                FastOutLinearInEasing.transform(scrollBehavior.state.overlappedFraction),
            )
    ) {
        if (!state.showingSearch || !searchAtBottom) {
            AnimatedContent(
                targetState = state.showingSearch,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) { showingSearch ->
                if (showingSearch) {
                    SearchBar(
                        showingSearch = state.showingSearch,
                        state = searchState,
                        onBack = { onAction(NetworkListViewModel.Action.ToggleSearch) },
                        placeholder = stringResource(R.string.search_hint),
                        actions = {
                            SortFilterButton(
                                filters = state.filters,
                                sortBy = state.sortBy,
                                sortAscending = state.sortAscending,
                                onAction = onAction,
                            )
                        },
                    )
                } else {
                    TopAppBar(
                        title = {
                            if (state.savedNetworks.isNotEmpty()) {
                                Text(
                                    text =
                                        pluralStringResource(
                                            R.plurals.network_list_title,
                                            state.savedNetworks.size,
                                            state.savedNetworks.size,
                                        ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    autoSize =
                                        TextAutoSize.StepBased(
                                            minFontSize = 16.sp,
                                            maxFontSize = LocalTextStyle.current.fontSize,
                                        ),
                                )
                            }
                        },
                        actions = {
                            if (!state.showingSearch) {
                                TooltipIconButton(
                                    onClick = {
                                        onAction(NetworkListViewModel.Action.ToggleSearch)
                                    },
                                    imageVector = Search,
                                    tooltip = stringResource(R.string.search_tooltip),
                                    positioning = TooltipAnchorPosition.Below,
                                )
                            }

                            SortFilterButton(
                                filters = state.filters,
                                sortBy = state.sortBy,
                                sortAscending = state.sortAscending,
                                onAction = onAction,
                            )

                            TooltipIconButton(
                                onClick = { navBackStack.add(Route.SettingScreen) },
                                imageVector = Settings,
                                tooltip = stringResource(R.string.settings_tooltip),
                                positioning = TooltipAnchorPosition.Below,
                            )
                        },
                        scrollBehavior = scrollBehavior,
                        colors =
                            topBarColors.copy(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Color.Transparent,
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
private fun NetworkListTopBarPreview() {
    NetworkListTopBar(
        state = NetworkListViewModel.State(savedNetworks = WifiNetwork.MOCK),
        searchState = rememberTextFieldState(),
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
        onAction = {},
    )
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun SearchNetworkListTopBarPreview() {
    NetworkListTopBar(
        state = NetworkListViewModel.State(showingSearch = true, savedNetworks = WifiNetwork.MOCK),
        searchState = rememberTextFieldState(),
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
        onAction = {},
    )
}
