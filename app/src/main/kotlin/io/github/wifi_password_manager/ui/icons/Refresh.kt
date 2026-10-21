package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Refresh: ImageVector
    get() {
        if (refresh != null) {
            return refresh!!
        }
        refresh =
            ImageVector.Builder(
                    name = "refresh",
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
                        moveTo(12f, 20f)
                        quadTo(8.65f, 20f, 6.33f, 17.68f)
                        reflectiveQuadTo(4f, 12f)
                        reflectiveQuadTo(6.33f, 6.32f)
                        reflectiveQuadTo(12f, 4f)
                        quadToRelative(1.73f, 0f, 3.3f, 0.71f)
                        quadTo(16.88f, 5.43f, 18f, 6.75f)
                        verticalLineTo(4f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(7f)
                        horizontalLineTo(13f)
                        verticalLineTo(9f)
                        horizontalLineToRelative(4.2f)
                        quadTo(16.4f, 7.6f, 15.01f, 6.8f)
                        reflectiveQuadTo(12f, 6f)
                        quadTo(9.5f, 6f, 7.75f, 7.75f)
                        reflectiveQuadTo(6f, 12f)
                        reflectiveQuadToRelative(1.75f, 4.25f)
                        reflectiveQuadTo(12f, 18f)
                        quadToRelative(1.93f, 0f, 3.48f, -1.1f)
                        reflectiveQuadTo(17.65f, 14f)
                        horizontalLineToRelative(2.1f)
                        quadToRelative(-0.7f, 2.65f, -2.85f, 4.32f)
                        reflectiveQuadTo(12f, 20f)
                        close()
                    }
                }
                .build()
        return refresh!!
    }

private var refresh: ImageVector? = null
