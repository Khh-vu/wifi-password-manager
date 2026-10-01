package io.github.wifi_password_manager.ui.screen.network.list.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.theme.SurfaceWrapper

@Composable
fun FilterRow(
    modifier: Modifier = Modifier,
    filter: NetworkListViewModel.Filter,
    onAction: (NetworkListViewModel.Action) -> Unit,
) {
    Surface(modifier = modifier.fillMaxWidth()) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            NetworkListViewModel.Filter.entries.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == filter,
                    onClick = { onAction(NetworkListViewModel.Action.FilterChanged(option)) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = NetworkListViewModel.Filter.entries.size,
                    ),
                    label = { Text(text = stringResource(option.labelResId)) },
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(SurfaceWrapper::class)
private fun FilterRowPreview() {
    FilterRow(filter = NetworkListViewModel.Filter.entries.random(), onAction = {})
}
