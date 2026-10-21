package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Close: ImageVector
    get() {
        if (close != null) {
            return close!!
        }
        close =
            ImageVector.Builder(
                    name = "close",
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
                        moveTo(6.4f, 19f)
                        lineTo(5f, 17.6f)
                        lineTo(10.6f, 12f)
                        lineTo(5f, 6.4f)
                        lineTo(6.4f, 5f)
                        lineTo(12f, 10.6f)
                        lineTo(17.6f, 5f)
                        lineTo(19f, 6.4f)
                        lineTo(13.4f, 12f)
                        lineTo(19f, 17.6f)
                        lineTo(17.6f, 19f)
                        lineTo(12f, 13.4f)
                        lineTo(6.4f, 19f)
                        close()
                    }
                }
                .build()
        return close!!
    }

private var close: ImageVector? = null
