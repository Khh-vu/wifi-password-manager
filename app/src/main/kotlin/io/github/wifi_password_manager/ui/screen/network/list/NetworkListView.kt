package io.github.wifi_password_manager.ui.screen.network.list

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
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
import io.github.wifi_password_manager.ui.LocalSettings
import io.github.wifi_password_manager.ui.icons.ArrowUpward
import io.github.wifi_password_manager.ui.screen.network.list.components.ExportDialog
import io.github.wifi_password_manager.ui.screen.network.list.components.ForgetAllDialog
import io.github.wifi_password_manager.ui.screen.network.list.components.ImportOptionDialog
import io.github.wifi_password_manager.ui.screen.network.list.components.ImportPasswordDialog
import io.github.wifi_password_manager.ui.screen.network.list.components.NetworkList
import io.github.wifi_password_manager.ui.screen.network.list.components.NetworkListToolbar
import io.github.wifi_password_manager.ui.screen.network.list.components.NetworkListTopBar
import io.github.wifi_password_manager.ui.screen.network.list.components.SortFilterButton
import io.github.wifi_password_manager.ui.shared.LoadingDialog
import io.github.wifi_password_manager.ui.shared.SearchBar
import io.github.wifi_password_manager.ui.theme.ThemeWrapper
import io.github.wifi_password_manager.utils.MOCK
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NetworkListView(
    state: NetworkListViewModel.State,
    searchState: TextFieldState,
    onAction: (NetworkListViewModel.Action) -> Unit,
    onScanQr: () -> Unit,
) {
    val searchAtBottom = LocalSettings.current.searchBarAtBottom

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val gridState = rememberLazyGridState()
    val pullToRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()
    val showScrollToTop by remember { derivedStateOf { gridState.firstVisibleItemIndex > 3 } }

    val order = Triple(state.filters, state.sortBy, state.sortAscending)
    var lastOrder by retain { mutableStateOf(order) }

    val insetsPaddingModifier =
        if (searchAtBottom) {
            Modifier
        } else {
            val insets = WindowInsets.ime.exclude(WindowInsets.navigationBars)
            Modifier.windowInsetsPadding(insets)
        }

    val toolbarPadding =
        if (state.showingSearch) {
            PaddingValues()
        } else {
            PaddingValues(
                bottom =
                    FloatingToolbarDefaults.ContainerSize + FloatingToolbarDefaults.ScreenOffset,
            )
        }

    LaunchedEffect(order) {
        if (order != lastOrder) {
            lastOrder = order
            gridState.scrollToItem(0)
        }
    }

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
        bottomBar = {
            if (searchAtBottom) {
                AnimatedVisibility(visible = state.showingSearch) {
                    SearchBar(
                        showingSearch = state.showingSearch,
                        state = searchState,
                        onBack = { onAction(NetworkListViewModel.Action.ToggleSearch) },
                        placeholder = stringResource(R.string.search_hint),
                        atBottom = true,
                        actions = {
                            SortFilterButton(
                                filters = state.filters,
                                sortBy = state.sortBy,
                                sortAscending = state.sortAscending,
                                onAction = onAction,
                            )
                        },
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showScrollToTop,
                modifier = Modifier.padding(toolbarPadding).then(insetsPaddingModifier),
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
            ) {
                TooltipBox(
                    positionProvider =
                        TooltipDefaults.rememberTooltipPositionProvider(
                            positioning = TooltipAnchorPosition.Above,
                        ),
                    tooltip = {
                        PlainTooltip {
                            Text(text = stringResource(R.string.scroll_to_top_description))
                        }
                    },
                    state = rememberTooltipState(),
                ) {
                    SmallFloatingActionButton(
                        onClick = { scope.launch { gridState.animateScrollToItem(0) } },
                    ) {
                        Icon(
                            imageVector = ArrowUpward,
                            contentDescription = stringResource(R.string.scroll_to_top_description),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        val modifier =
            Modifier.fillMaxSize()
                .then(
                    when {
                        searchAtBottom && state.showingSearch -> Modifier.statusBarsPadding()
                        !searchAtBottom -> Modifier.imePadding()
                        else -> Modifier
                    },
                )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(NetworkListViewModel.Action.Refresh) },
            modifier = modifier,
            state = pullToRefreshState,
            enabled = !state.isCacheMode,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullToRefreshState,
                    isRefreshing = state.isRefreshing,
                    modifier =
                        Modifier.align(Alignment.TopCenter)
                            .padding(top = innerPadding.calculateTopPadding()),
                )
            },
        ) {
            if (state.savedNetworks.isEmpty()) {
                Box(
                    modifier =
                        Modifier.fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center,
                ) {
                    val isFiltering = state.showingSearch || state.filters.isNotEmpty()
                    Text(
                        text =
                            stringResource(
                                if (isFiltering) R.string.no_networks_found
                                else R.string.no_networks_available,
                            ),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }
            } else {
                NetworkList(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding,
                    toolbarPadding = toolbarPadding,
                    gridState = gridState,
                    networks = state.savedNetworks,
                    connectionStatus = state.connectionStatus,
                    isCacheMode = state.isCacheMode,
                    onAction = onAction,
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).then(insetsPaddingModifier),
        ) {
            AnimatedVisibility(
                visible = !state.showingSearch,
                modifier =
                    Modifier.align(Alignment.BottomCenter)
                        .padding(FloatingToolbarDefaults.ScreenOffset),
            ) {
                NetworkListToolbar(
                    state = state,
                    onAction = onAction,
                    onScanQr = onScanQr,
                )
            }
        }
    }

    when {
        state.isLoading -> {
            LoadingDialog()
        }

        state.showForgetAllDialog -> {
            ForgetAllDialog(
                onDismiss = { onAction(NetworkListViewModel.Action.HideForgetAllDialog) },
                onConfirm = { onAction(NetworkListViewModel.Action.ConfirmForgetAllNetworks(it)) },
            )
        }

        state.showExportDialog -> {
            ExportDialog(
                onDismiss = { onAction(NetworkListViewModel.Action.HideExportDialog) },
                onSelect = { option, password ->
                    onAction(NetworkListViewModel.Action.ConfirmExport(option, password))
                },
            )
        }

        state.showImportModeDialog -> {
            ImportOptionDialog(
                onDismiss = { onAction(NetworkListViewModel.Action.HideImportDialog) },
                onSelect = { onAction(NetworkListViewModel.Action.ChooseImportMode(it)) },
            )
        }

        state.showImportPasswordDialog -> {
            ImportPasswordDialog(
                onDismiss = { onAction(NetworkListViewModel.Action.HideImportDialog) },
                onConfirm = { password ->
                    onAction(NetworkListViewModel.Action.ConfirmImportWithPassword(password))
                },
            )
        }
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
        onScanQr = {},
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
        onScanQr = {},
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
        onScanQr = {},
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
        onScanQr = {},
    )
}
