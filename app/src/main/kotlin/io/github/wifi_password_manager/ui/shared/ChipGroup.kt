package io.github.wifi_password_manager.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.domain.model.ExportOption
import io.github.wifi_password_manager.ui.icons.Check
import io.github.wifi_password_manager.ui.theme.ThemeWrapper

@Composable
fun <T> ChipGroup(
    options: List<T>,
    selected: (T) -> Boolean,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    selectedIcon: (T) -> ImageVector = { Check },
) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val leadingIcon =
                @Composable {
                    Icon(
                        imageVector = selectedIcon(option),
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                }
            FilterChip(
                selected = selected(option),
                onClick = { onSelect(option) },
                label = { Text(text = label(option)) },
                leadingIcon = leadingIcon.takeIf { selected(option) },
            )
        }
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun ChipGroupPreview() {
    ChipGroup(
        options = ExportOption.entries,
        selected = { it == ExportOption.entries.random() },
        label = { stringResource(it.titleResId) },
        onSelect = {},
    )
}
