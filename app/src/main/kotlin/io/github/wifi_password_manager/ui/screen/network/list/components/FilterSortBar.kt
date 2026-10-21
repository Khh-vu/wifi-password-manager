package io.github.wifi_password_manager.ui.screen.network.list.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.icons.ArrowDownward
import io.github.wifi_password_manager.ui.icons.ArrowUpward
import io.github.wifi_password_manager.ui.icons.Check
import io.github.wifi_password_manager.ui.icons.FilterAlt
import io.github.wifi_password_manager.ui.icons.KeyboardArrowDown
import io.github.wifi_password_manager.ui.icons.Sort
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.theme.SurfaceWrapper
import kotlin.random.Random

@Composable
fun FilterSortBar(
    modifier: Modifier = Modifier,
    filter: NetworkListViewModel.Filter,
    sortAscending: Boolean,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    var expanded by retain { mutableStateOf(false) }
    val rotationAngle by
        animateFloatAsState(
            targetValue = if (expanded) 180f else 0f,
            animationSpec = tween(durationMillis = 300),
        )

    Surface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box {
                FilterChip(
                    selected = filter != NetworkListViewModel.Filter.ALL,
                    onClick = { expanded = !expanded },
                    label = { Text(text = stringResource(filter.labelResId), maxLines = 1) },
                    leadingIcon = {
                        Icon(
                            imageVector = FilterAlt,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = KeyboardArrowDown,
                            contentDescription =
                                if (expanded) {
                                    stringResource(R.string.collapse_description)
                                } else {
                                    stringResource(R.string.expand_description)
                                },
                            modifier =
                                Modifier.size(FilterChipDefaults.IconSize).graphicsLayer {
                                    rotationZ = rotationAngle
                                },
                        )
                    },
                )

                DropdownMenuPopup(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                        NetworkListViewModel.Filter.entries.forEachIndexed { index, option ->
                            SelectableDropdownMenuItem(
                                selected = option == filter,
                                onClick = {
                                    expanded = false
                                    onAction(NetworkListViewModel.Action.FilterChanged(option))
                                },
                                text = { Text(text = stringResource(option.labelResId)) },
                                selectedLeadingIcon = {
                                    Icon(imageVector = Check, contentDescription = null)
                                },
                                shapes =
                                    MenuDefaults.itemShape(
                                        index = index,
                                        count = NetworkListViewModel.Filter.entries.size,
                                    ),
                                colors =
                                    MenuDefaults.selectableItemColors(
                                        selectedContainerColor =
                                            MaterialTheme.colorScheme.primaryContainer,
                                        selectedTextColor =
                                            contentColorFor(
                                                MaterialTheme.colorScheme.primaryContainer
                                            ),
                                        selectedLeadingIconColor =
                                            contentColorFor(
                                                MaterialTheme.colorScheme.primaryContainer
                                            ),
                                    ),
                            )
                        }
                    }
                }
            }

            AssistChip(
                onClick = { onAction(NetworkListViewModel.Action.ToggleSort) },
                label = { Text(text = stringResource(R.string.ssid), maxLines = 1) },
                leadingIcon = {
                    Icon(
                        imageVector = Sort,
                        contentDescription = stringResource(R.string.sort_by),
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = if (sortAscending) ArrowUpward else ArrowDownward,
                        contentDescription =
                            stringResource(
                                if (sortAscending) R.string.sort_asc else R.string.sort_desc
                            ),
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                    )
                },
            )
        }
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun FilterSortBarPreview() {
    FilterSortBar(
        filter = NetworkListViewModel.Filter.entries.random(),
        sortAscending = Random.nextBoolean(),
        onAction = {},
    )
}
