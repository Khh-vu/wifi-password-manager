package io.github.wifi_password_manager.ui.shared

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import io.github.wifi_password_manager.ui.icons.Search
import io.github.wifi_password_manager.ui.theme.ThemeWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    shapes: IconButtonShapes = IconButtonDefaults.shapes(),
    imageVector: ImageVector,
    tooltip: String,
    positioning: TooltipAnchorPosition,
) {
    TooltipBox(
        positionProvider =
            TooltipDefaults.rememberTooltipPositionProvider(positioning = positioning),
        tooltip = { PlainTooltip { Text(text = tooltip) } },
        state = rememberTooltipState(),
    ) {
        IconButton(modifier = modifier, onClick = onClick, shapes = shapes) {
            Icon(imageVector = imageVector, contentDescription = tooltip)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun TooltipIconButtonPreview() {
    TooltipIconButton(
        onClick = {},
        imageVector = Search,
        tooltip = LoremIpsum(2).values.joinToString(" "),
        positioning = TooltipAnchorPosition.Below,
    )
}
