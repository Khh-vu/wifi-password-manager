package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ArrowDownward: ImageVector
    get() {
        if (arrowDownward != null) {
            return arrowDownward!!
        }
        arrowDownward =
            ImageVector.Builder(
                    name = "arrow_downward",
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
                        moveTo(11f, 4f)
                        verticalLineTo(16.18f)
                        lineTo(5.4f, 10.58f)
                        lineTo(4f, 12f)
                        lineToRelative(8f, 8f)
                        lineToRelative(8f, -8f)
                        lineTo(18.6f, 10.58f)
                        lineTo(13f, 16.18f)
                        verticalLineTo(4f)
                        horizontalLineTo(11f)
                        close()
                    }
                }
                .build()
        return arrowDownward!!
    }

private var arrowDownward: ImageVector? = null
