package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SwapVert: ImageVector
    get() {
        if (swapVert != null) {
            return swapVert!!
        }
        swapVert =
            ImageVector.Builder(
                    name = "swap_vert",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(8f, 13f)
                        verticalLineTo(5.82f)
                        lineTo(5.43f, 8.4f)
                        lineTo(4f, 7f)
                        lineTo(9f, 2f)
                        lineToRelative(5f, 5f)
                        lineTo(12.58f, 8.4f)
                        lineTo(10f, 5.82f)
                        verticalLineTo(13f)
                        horizontalLineTo(8f)
                        close()
                        moveToRelative(7f, 9f)
                        lineTo(10f, 17f)
                        lineToRelative(1.43f, -1.4f)
                        lineTo(14f, 18.18f)
                        verticalLineTo(11f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(7.18f)
                        lineTo(18.58f, 15.6f)
                        lineTo(20f, 17f)
                        lineToRelative(-5f, 5f)
                        close()
                    }
                }
                .build()
        return swapVert!!
    }

private var swapVert: ImageVector? = null
