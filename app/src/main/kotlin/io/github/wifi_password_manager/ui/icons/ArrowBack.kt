package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ArrowBack: ImageVector
    get() {
        if (arrowBack != null) {
            return arrowBack!!
        }
        arrowBack =
            ImageVector.Builder(
                    name = "arrow_back",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                    autoMirror = true,
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
                        moveTo(7.83f, 13f)
                        lineToRelative(5.6f, 5.6f)
                        lineTo(12f, 20f)
                        lineTo(4f, 12f)
                        lineTo(12f, 4f)
                        lineToRelative(1.43f, 1.4f)
                        lineTo(7.83f, 11f)
                        horizontalLineTo(20f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(7.83f)
                        close()
                    }
                }
                .build()
        return arrowBack!!
    }

private var arrowBack: ImageVector? = null
