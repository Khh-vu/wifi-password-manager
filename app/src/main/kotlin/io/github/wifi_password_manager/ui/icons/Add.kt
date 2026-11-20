package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Add: ImageVector
    get() {
        if (add != null) {
            return add!!
        }
        add =
            ImageVector.Builder(
                    name = "add",
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
                        moveTo(11f, 13f)
                        horizontalLineTo(5f)
                        verticalLineTo(11f)
                        horizontalLineToRelative(6f)
                        verticalLineTo(5f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(6f)
                        horizontalLineToRelative(6f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(13f)
                        verticalLineToRelative(6f)
                        horizontalLineTo(11f)
                        verticalLineTo(13f)
                        close()
                    }
                }
                .build()
        return add!!
    }

private var add: ImageVector? = null
