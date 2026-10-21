package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val IntegrationInstructions: ImageVector
    get() {
        if (integrationInstructions != null) {
            return integrationInstructions!!
        }
        integrationInstructions =
            ImageVector.Builder(
                    name = "integration_instructions",
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
                        moveTo(9.6f, 15.6f)
                        lineTo(11f, 14.15f)
                        lineTo(8.85f, 12f)
                        lineTo(11f, 9.85f)
                        lineTo(9.6f, 8.4f)
                        lineTo(6f, 12f)
                        lineToRelative(3.6f, 3.6f)
                        close()
                        moveToRelative(4.8f, 0f)
                        lineTo(18f, 12f)
                        lineTo(14.4f, 8.4f)
                        lineTo(13f, 9.85f)
                        lineTo(15.15f, 12f)
                        lineTo(13f, 14.15f)
                        lineToRelative(1.4f, 1.45f)
                        close()
                        moveTo(5f, 21f)
                        quadTo(4.18f, 21f, 3.59f, 20.41f)
                        reflectiveQuadTo(3f, 19f)
                        verticalLineTo(5f)
                        quadTo(3f, 4.17f, 3.59f, 3.59f)
                        reflectiveQuadTo(5f, 3f)
                        horizontalLineTo(9.2f)
                        quadTo(9.53f, 2.1f, 10.29f, 1.55f)
                        reflectiveQuadTo(12f, 1f)
                        reflectiveQuadToRelative(1.71f, 0.55f)
                        reflectiveQuadTo(14.8f, 3f)
                        horizontalLineTo(19f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        reflectiveQuadTo(21f, 5f)
                        verticalLineTo(19f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(19f, 21f)
                        horizontalLineTo(5f)
                        close()
                        moveTo(5f, 19f)
                        horizontalLineTo(19f)
                        verticalLineTo(5f)
                        horizontalLineTo(5f)
                        verticalLineTo(19f)
                        close()
                        moveTo(12.54f, 4.04f)
                        quadTo(12.75f, 3.82f, 12.75f, 3.5f)
                        quadToRelative(0f, -0.33f, -0.21f, -0.54f)
                        reflectiveQuadTo(12f, 2.75f)
                        reflectiveQuadTo(11.46f, 2.96f)
                        reflectiveQuadTo(11.25f, 3.5f)
                        quadToRelative(0f, 0.32f, 0.21f, 0.54f)
                        reflectiveQuadTo(12f, 4.25f)
                        reflectiveQuadTo(12.54f, 4.04f)
                        close()
                        moveTo(5f, 19f)
                        verticalLineTo(5f)
                        verticalLineTo(19f)
                        close()
                    }
                }
                .build()
        return integrationInstructions!!
    }

private var integrationInstructions: ImageVector? = null
