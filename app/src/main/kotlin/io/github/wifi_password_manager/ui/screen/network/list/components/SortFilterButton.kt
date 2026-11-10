package io.github.wifi_password_manager.ui.screen.network.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.icons.ArrowDownward
import io.github.wifi_password_manager.ui.icons.ArrowUpward
import io.github.wifi_password_manager.ui.icons.FilterAlt
import io.github.wifi_password_manager.ui.icons.History
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.shared.ChipGroup
import io.github.wifi_password_manager.ui.shared.TooltipIconButton
import io.github.wifi_password_manager.ui.theme.SurfaceWrapper
import io.github.wifi_password_manager.ui.theme.ThemeWrapper
import io.github.wifi_password_manager.utils.plus
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFilterButton(
    filters: Set<NetworkListViewModel.Filter>,
    sortBy: NetworkListViewModel.SortBy,
    sortAscending: Boolean,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    var showBottomSheet by retain { mutableStateOf(false) }
    val sheetState =
        rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        )
    val isDefault = filters.isEmpty() && sortBy == NetworkListViewModel.SortBy.SSID && sortAscending

    TooltipIconButton(
        onClick = { showBottomSheet = true },
        tooltip = stringResource(R.string.sort_filter_title),
        positioning = TooltipAnchorPosition.Below,
        icon = {
            BadgedBox(
                badge = {
                    if (!isDefault) Badge(containerColor = MaterialTheme.colorScheme.primary)
                },
            ) {
                Icon(
                    imageVector = FilterAlt,
                    contentDescription = stringResource(R.string.sort_filter_title),
                )
            }
        },
    )

    if (showBottomSheet) {
        SortFilterSheet(
            sheetState = sheetState,
            filters = filters,
            sortBy = sortBy,
            sortAscending = sortAscending,
            canReset = !isDefault,
            onDismiss = { showBottomSheet = false },
            onAction = onAction,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortFilterSheet(
    sheetState: SheetState,
    filters: Set<NetworkListViewModel.Filter>,
    sortBy: NetworkListViewModel.SortBy,
    sortAscending: Boolean,
    canReset: Boolean,
    onDismiss: () -> Unit,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.sort_filter_title),
                style = MaterialTheme.typography.titleMedium,
            )

            TooltipIconButton(
                onClick = { onAction(NetworkListViewModel.Action.ResetSortFilter) },
                enabled = canReset,
                imageVector = History,
                tooltip = stringResource(R.string.reset),
                positioning = TooltipAnchorPosition.Above,
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 12.dp) + PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.sort_by),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )

                ChipGroup(
                    options = NetworkListViewModel.SortBy.entries,
                    selected = { it == sortBy },
                    label = { stringResource(it.labelResId) },
                    onSelect = { onAction(NetworkListViewModel.Action.SortChanged(it)) },
                )

                if (
                    sortBy in
                        setOf(
                            NetworkListViewModel.SortBy.CREATED,
                            NetworkListViewModel.SortBy.UPDATED,
                        )
                ) {
                    Text(
                        text = stringResource(R.string.sort_time_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.sort_direction),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )

                ChipGroup(
                    options = listOf(true, false),
                    selected = { it == sortAscending },
                    label = { stringResource(if (it) R.string.sort_asc else R.string.sort_desc) },
                    onSelect = { onAction(NetworkListViewModel.Action.SortDirectionChanged(it)) },
                    selectedIcon = { if (it) ArrowUpward else ArrowDownward },
                )
            }

            item { HorizontalDivider() }

            items(NetworkListViewModel.Filter.Group.entries) { group ->
                Text(
                    text = stringResource(group.labelResId),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )

                ChipGroup(
                    options = NetworkListViewModel.Filter.entries.filter { it.group == group },
                    selected = { it in filters },
                    label = { stringResource(it.labelResId) },
                    onSelect = { onAction(NetworkListViewModel.Action.FilterToggled(it)) },
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun SortFilterButtonPreview() {
    SortFilterButton(
        filters = NetworkListViewModel.Filter.entries.filter { Random.nextBoolean() }.toSet(),
        sortBy = NetworkListViewModel.SortBy.entries.random(),
        sortAscending = Random.nextBoolean(),
        onAction = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun SortFilterSheetPreview() {
    SortFilterSheet(
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Expanded),
        filters = NetworkListViewModel.Filter.entries.filter { Random.nextBoolean() }.toSet(),
        sortBy = NetworkListViewModel.SortBy.entries.random(),
        sortAscending = Random.nextBoolean(),
        canReset = Random.nextBoolean(),
        onDismiss = {},
        onAction = {},
    )
}
