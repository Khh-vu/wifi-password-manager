package io.github.wifi_password_manager.ui.shared

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TooltipIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    shapes: IconButtonShapes = IconButtonDefaults.shapes(),
    painter: Painter,
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
            Icon(painter = painter, contentDescription = tooltip)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@PreviewLightDark
@Composable
private fun TooltipIconButtonPreview() {
    WiFiPasswordManagerTheme {
        Surface {
            TooltipIconButton(
                onClick = {},
                painter = painterResource(R.drawable.ic_search),
                tooltip = "Tooltip",
                positioning = TooltipAnchorPosition.Below,
            )
        }
    }
}
