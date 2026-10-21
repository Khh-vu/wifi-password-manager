package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val EditNote: ImageVector
    get() {
        if (editNote != null) {
            return editNote!!
        }
        editNote =
            ImageVector.Builder(
                    name = "edit_note",
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
                        moveTo(4f, 14f)
                        verticalLineTo(12f)
                        horizontalLineToRelative(7f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(4f)
                        close()
                        moveTo(4f, 10f)
                        verticalLineTo(8f)
                        horizontalLineTo(15f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(4f)
                        close()
                        moveTo(4f, 6f)
                        verticalLineTo(4f)
                        horizontalLineTo(15f)
                        verticalLineTo(6f)
                        horizontalLineTo(4f)
                        close()
                        moveToRelative(9f, 14f)
                        verticalLineTo(16.93f)
                        lineToRelative(5.53f, -5.5f)
                        quadToRelative(0.22f, -0.22f, 0.5f, -0.32f)
                        reflectiveQuadTo(19.58f, 11f)
                        quadToRelative(0.3f, 0f, 0.57f, 0.11f)
                        quadToRelative(0.27f, 0.11f, 0.5f, 0.34f)
                        lineToRelative(0.93f, 0.93f)
                        quadToRelative(0.2f, 0.22f, 0.31f, 0.5f)
                        reflectiveQuadTo(22f, 13.43f)
                        reflectiveQuadToRelative(-0.1f, 0.56f)
                        reflectiveQuadTo(21.58f, 14.5f)
                        lineTo(16.08f, 20f)
                        horizontalLineTo(13f)
                        close()
                        moveToRelative(7.5f, -6.58f)
                        lineTo(19.58f, 12.5f)
                        lineToRelative(0.92f, 0.92f)
                        close()
                        moveToRelative(-6f, 5.08f)
                        horizontalLineToRelative(0.95f)
                        lineToRelative(3.03f, -3.05f)
                        lineTo(18.03f, 14.98f)
                        lineTo(17.55f, 14.53f)
                        lineTo(14.5f, 17.55f)
                        verticalLineTo(18.5f)
                        close()
                        moveToRelative(3.53f, -3.53f)
                        lineTo(17.55f, 14.53f)
                        lineToRelative(0.93f, 0.92f)
                        lineTo(18.03f, 14.98f)
                        close()
                    }
                }
                .build()
        return editNote!!
    }

private var editNote: ImageVector? = null
