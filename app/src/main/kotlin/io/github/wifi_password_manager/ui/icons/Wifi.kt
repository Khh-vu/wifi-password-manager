package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Wifi: ImageVector
    get() {
        if (wifi != null) {
            return wifi!!
        }
        wifi =
            ImageVector.Builder(
                    name = "wifi",
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
                        moveTo(10.23f, 20.27f)
                        quadTo(9.5f, 19.55f, 9.5f, 18.5f)
                        reflectiveQuadToRelative(0.73f, -1.77f)
                        reflectiveQuadTo(12f, 16f)
                        reflectiveQuadToRelative(1.78f, 0.73f)
                        reflectiveQuadTo(14.5f, 18.5f)
                        reflectiveQuadToRelative(-0.72f, 1.77f)
                        reflectiveQuadTo(12f, 21f)
                        reflectiveQuadTo(10.23f, 20.27f)
                        close()
                        moveTo(6.35f, 15.35f)
                        lineTo(4.25f, 13.2f)
                        quadTo(5.73f, 11.73f, 7.71f, 10.86f)
                        reflectiveQuadTo(12f, 10f)
                        reflectiveQuadToRelative(4.29f, 0.88f)
                        reflectiveQuadToRelative(3.46f, 2.38f)
                        lineToRelative(-2.1f, 2.1f)
                        quadToRelative(-1.1f, -1.1f, -2.55f, -1.72f)
                        reflectiveQuadTo(12f, 13f)
                        reflectiveQuadTo(8.9f, 13.63f)
                        reflectiveQuadTo(6.35f, 15.35f)
                        close()
                        moveTo(2.1f, 11.1f)
                        lineTo(0f, 9f)
                        quadTo(2.3f, 6.65f, 5.38f, 5.32f)
                        reflectiveQuadTo(12f, 4f)
                        reflectiveQuadToRelative(6.63f, 1.32f)
                        reflectiveQuadTo(24f, 9f)
                        lineToRelative(-2.1f, 2.1f)
                        quadTo(19.98f, 9.17f, 17.44f, 8.09f)
                        reflectiveQuadTo(12f, 7f)
                        reflectiveQuadTo(6.56f, 8.09f)
                        reflectiveQuadTo(2.1f, 11.1f)
                        close()
                    }
                }
                .build()
        return wifi!!
    }

private var wifi: ImageVector? = null
