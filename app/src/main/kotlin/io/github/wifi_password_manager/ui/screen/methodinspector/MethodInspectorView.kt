package io.github.wifi_password_manager.ui.screen.methodinspector

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.icons.Download
import io.github.wifi_password_manager.ui.icons.Search
import io.github.wifi_password_manager.ui.shared.BackButton
import io.github.wifi_password_manager.ui.shared.ExpansionItem
import io.github.wifi_password_manager.ui.shared.LoadingDialog
import io.github.wifi_password_manager.ui.shared.SearchBar
import io.github.wifi_password_manager.ui.shared.TooltipIconButton
import io.github.wifi_password_manager.ui.theme.ThemeWrapper
import io.github.wifi_password_manager.utils.plus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MethodInspectorView(
    state: MethodInspectorViewModel.State,
    searchState: TextFieldState,
    onAction: (MethodInspectorViewModel.Action) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    BackHandler(enabled = state.showingSearch) {
        onAction(MethodInspectorViewModel.Action.ToggleSearch)
    }

    Scaffold(
        topBar = {
            AnimatedContent(
                targetState = state.showingSearch,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) { showingSearch ->
                if (showingSearch) {
                    SearchBar(
                        showingSearch = state.showingSearch,
                        state = searchState,
                        onBack = { onAction(MethodInspectorViewModel.Action.ToggleSearch) },
                        placeholder = stringResource(R.string.method_inspector_search_hint),
                    )
                } else {
                    TopAppBar(
                        navigationIcon = { BackButton() },
                        title = {
                            Text(
                                text =
                                    pluralStringResource(
                                        R.plurals.method_inspector_title,
                                        state.methods.size,
                                        state.methods.size,
                                    )
                            )
                        },
                        actions = {
                            TooltipIconButton(
                                onClick = {
                                    onAction(MethodInspectorViewModel.Action.ToggleSearch)
                                },
                                imageVector = Search,
                                tooltip = stringResource(R.string.search_tooltip),
                                positioning = TooltipAnchorPosition.Below,
                            )
                        },
                        scrollBehavior = scrollBehavior,
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.imePadding(),
                text = { Text(text = stringResource(R.string.export_methods_action)) },
                icon = {
                    Icon(
                        imageVector = Download,
                        contentDescription = stringResource(R.string.export_methods_action),
                    )
                },
                onClick = { onAction(MethodInspectorViewModel.Action.ExportMethods) },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = innerPadding + PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = state.methods, key = { (name, _) -> name }) { (name, signature) ->
                Card {
                    ExpansionItem(
                        content = {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            ) {
                                Text(text = name)
                            }
                        }
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        ) {
                            Text(text = signature)
                        }
                    }
                }
            }
        }
    }

    if (state.isExporting) {
        LoadingDialog()
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun MethodInspectorViewPreview() {
    MethodInspectorView(
        state =
            MethodInspectorViewModel.State(
                methods =
                    listOf(
                        "method1" to "signature1",
                        "method2" to "signature2",
                        "method3" to "signature3",
                    )
            ),
        searchState = rememberTextFieldState(),
        onAction = {},
    )
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun SearchMethodInspectorViewPreview() {
    MethodInspectorView(
        state = MethodInspectorViewModel.State(showingSearch = true),
        searchState = rememberTextFieldState(),
        onAction = {},
    )
}
