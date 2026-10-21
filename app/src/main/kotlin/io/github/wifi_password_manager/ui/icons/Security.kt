package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Security: ImageVector
    get() {
        if (security != null) {
            return security!!
        }
        security =
            ImageVector.Builder(
                    name = "security",
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
                        moveTo(12f, 22f)
                        quadTo(8.53f, 21.13f, 6.26f, 18.01f)
                        reflectiveQuadTo(4f, 11.1f)
                        verticalLineTo(5f)
                        lineTo(12f, 2f)
                        lineToRelative(8f, 3f)
                        verticalLineToRelative(6.1f)
                        quadToRelative(0f, 3.8f, -2.26f, 6.91f)
                        reflectiveQuadTo(12f, 22f)
                        close()
                        moveToRelative(0f, -2.1f)
                        quadToRelative(2.43f, -0.75f, 4.05f, -2.96f)
                        reflectiveQuadTo(17.95f, 12f)
                        horizontalLineTo(12f)
                        verticalLineTo(4.13f)
                        lineTo(6f, 6.38f)
                        verticalLineTo(11.1f)
                        quadToRelative(0f, 0.28f, 0f, 0.45f)
                        quadTo(6f, 11.73f, 6.05f, 12f)
                        horizontalLineTo(12f)
                        verticalLineToRelative(7.9f)
                        close()
                    }
                }
                .build()
        return security!!
    }

private var security: ImageVector? = null
