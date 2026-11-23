package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Upload: ImageVector
    get() {
        if (upload != null) {
            return upload!!
        }
        upload =
            ImageVector.Builder(
                    name = "upload",
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
                        moveTo(11f, 16f)
                        verticalLineTo(7.85f)
                        lineTo(8.4f, 10.45f)
                        lineTo(7f, 9f)
                        lineToRelative(5f, -5f)
                        lineToRelative(5f, 5f)
                        lineToRelative(-1.4f, 1.45f)
                        lineToRelative(-2.6f, -2.6f)
                        verticalLineTo(16f)
                        horizontalLineTo(11f)
                        close()
                        moveTo(6f, 20f)
                        quadTo(5.18f, 20f, 4.59f, 19.41f)
                        reflectiveQuadTo(4f, 18f)
                        verticalLineTo(15f)
                        horizontalLineTo(6f)
                        verticalLineToRelative(3f)
                        horizontalLineTo(18f)
                        verticalLineTo(15f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(3f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(18f, 20f)
                        horizontalLineTo(6f)
                        close()
                    }
                }
                .build()
        return upload!!
    }

private var upload: ImageVector? = null
