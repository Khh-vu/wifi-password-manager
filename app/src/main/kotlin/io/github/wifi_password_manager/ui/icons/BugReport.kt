package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BugReport: ImageVector
    get() {
        if (bugReport != null) {
            return bugReport!!
        }
        bugReport =
            ImageVector.Builder(
                    name = "bug_report",
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
                        moveTo(12f, 19f)
                        quadToRelative(1.65f, 0f, 2.83f, -1.18f)
                        reflectiveQuadTo(16f, 15f)
                        verticalLineTo(11f)
                        quadTo(16f, 9.35f, 14.83f, 8.17f)
                        reflectiveQuadTo(12f, 7f)
                        reflectiveQuadTo(9.18f, 8.17f)
                        reflectiveQuadTo(8f, 11f)
                        verticalLineToRelative(4f)
                        quadToRelative(0f, 1.65f, 1.18f, 2.82f)
                        reflectiveQuadTo(12f, 19f)
                        close()
                        moveTo(10f, 16f)
                        horizontalLineToRelative(4f)
                        verticalLineTo(14f)
                        horizontalLineTo(10f)
                        verticalLineToRelative(2f)
                        close()
                        moveToRelative(0f, -4f)
                        horizontalLineToRelative(4f)
                        verticalLineTo(10f)
                        horizontalLineTo(10f)
                        verticalLineToRelative(2f)
                        close()
                        moveToRelative(2f, 1f)
                        close()
                        moveToRelative(0f, 8f)
                        quadTo(10.38f, 21f, 8.99f, 20.2f)
                        reflectiveQuadTo(6.8f, 18f)
                        horizontalLineTo(4f)
                        verticalLineTo(16f)
                        horizontalLineTo(6.1f)
                        quadTo(6.03f, 15.5f, 6.01f, 15f)
                        reflectiveQuadTo(6f, 14f)
                        horizontalLineTo(4f)
                        verticalLineTo(12f)
                        horizontalLineTo(6f)
                        quadTo(6f, 11.5f, 6.01f, 11f)
                        reflectiveQuadTo(6.1f, 10f)
                        horizontalLineTo(4f)
                        verticalLineTo(8f)
                        horizontalLineTo(6.8f)
                        quadTo(7.15f, 7.43f, 7.59f, 6.93f)
                        reflectiveQuadTo(8.6f, 6.05f)
                        lineTo(7f, 4.4f)
                        lineTo(8.4f, 3f)
                        lineToRelative(2.15f, 2.15f)
                        quadToRelative(0.7f, -0.22f, 1.43f, -0.22f)
                        reflectiveQuadTo(13.4f, 5.15f)
                        lineTo(15.6f, 3f)
                        lineTo(17f, 4.4f)
                        lineTo(15.35f, 6.05f)
                        quadToRelative(0.57f, 0.38f, 1.04f, 0.86f)
                        reflectiveQuadTo(17.2f, 8f)
                        horizontalLineTo(20f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(17.9f)
                        quadToRelative(0.07f, 0.5f, 0.09f, 1f)
                        reflectiveQuadTo(18f, 12f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(18f)
                        quadToRelative(0f, 0.5f, -0.01f, 1f)
                        reflectiveQuadTo(17.9f, 16f)
                        horizontalLineTo(20f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(17.2f)
                        quadToRelative(-0.8f, 1.4f, -2.19f, 2.2f)
                        reflectiveQuadTo(12f, 21f)
                        close()
                    }
                }
                .build()
        return bugReport!!
    }

private var bugReport: ImageVector? = null
