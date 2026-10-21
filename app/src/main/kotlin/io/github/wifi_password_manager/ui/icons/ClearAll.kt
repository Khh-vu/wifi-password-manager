package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ClearAll: ImageVector
    get() {
        if (clearAll != null) {
            return clearAll!!
        }
        clearAll =
            ImageVector.Builder(
                    name = "clear_all",
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
                        moveTo(3f, 17f)
                        verticalLineTo(15f)
                        horizontalLineTo(17f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(3f)
                        close()
                        moveTo(5f, 13f)
                        verticalLineTo(11f)
                        horizontalLineTo(19f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(5f)
                        close()
                        moveTo(7f, 9f)
                        verticalLineTo(7f)
                        horizontalLineTo(21f)
                        verticalLineTo(9f)
                        horizontalLineTo(7f)
                        close()
                    }
                }
                .build()
        return clearAll!!
    }

private var clearAll: ImageVector? = null
