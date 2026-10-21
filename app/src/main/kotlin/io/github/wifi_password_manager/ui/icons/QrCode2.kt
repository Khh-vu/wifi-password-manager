package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QrCode2: ImageVector
    get() {
        if (qrCode2 != null) {
            return qrCode2!!
        }
        qrCode2 =
            ImageVector.Builder(
                    name = "qr_code_2",
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
                        moveTo(13f, 21f)
                        verticalLineTo(19f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(13f)
                        close()
                        moveTo(11f, 19f)
                        verticalLineTo(14f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(5f)
                        horizontalLineTo(11f)
                        close()
                        moveToRelative(8f, -3f)
                        verticalLineTo(12f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(4f)
                        horizontalLineTo(19f)
                        close()
                        moveTo(17f, 12f)
                        verticalLineTo(10f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(17f)
                        close()
                        moveTo(5f, 14f)
                        verticalLineTo(12f)
                        horizontalLineTo(7f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(5f)
                        close()
                        moveTo(3f, 12f)
                        verticalLineTo(10f)
                        horizontalLineTo(5f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(3f)
                        close()
                        moveTo(12f, 5f)
                        verticalLineTo(3f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(5f)
                        horizontalLineTo(12f)
                        close()
                        moveTo(4.5f, 7.5f)
                        horizontalLineToRelative(3f)
                        verticalLineToRelative(-3f)
                        horizontalLineToRelative(-3f)
                        verticalLineToRelative(3f)
                        close()
                        moveTo(3f, 9f)
                        verticalLineTo(3f)
                        horizontalLineTo(9f)
                        verticalLineTo(9f)
                        horizontalLineTo(3f)
                        close()
                        moveTo(4.5f, 19.5f)
                        horizontalLineToRelative(3f)
                        verticalLineToRelative(-3f)
                        horizontalLineToRelative(-3f)
                        verticalLineToRelative(3f)
                        close()
                        moveTo(3f, 21f)
                        verticalLineTo(15f)
                        horizontalLineTo(9f)
                        verticalLineToRelative(6f)
                        horizontalLineTo(3f)
                        close()
                        moveTo(16.5f, 7.5f)
                        horizontalLineToRelative(3f)
                        verticalLineToRelative(-3f)
                        horizontalLineToRelative(-3f)
                        verticalLineToRelative(3f)
                        close()
                        moveTo(15f, 9f)
                        verticalLineTo(3f)
                        horizontalLineToRelative(6f)
                        verticalLineTo(9f)
                        horizontalLineTo(15f)
                        close()
                        moveToRelative(2f, 12f)
                        verticalLineTo(18f)
                        horizontalLineTo(15f)
                        verticalLineTo(16f)
                        horizontalLineToRelative(4f)
                        verticalLineToRelative(3f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(17f)
                        close()
                        moveTo(13f, 14f)
                        verticalLineTo(12f)
                        horizontalLineToRelative(4f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(13f)
                        close()
                        moveTo(9f, 14f)
                        verticalLineTo(12f)
                        horizontalLineTo(7f)
                        verticalLineTo(10f)
                        horizontalLineToRelative(6f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(11f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(9f)
                        close()
                        moveTo(10f, 9f)
                        verticalLineTo(5f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(7f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(9f)
                        horizontalLineTo(10f)
                        close()
                        moveTo(5.25f, 6.75f)
                        verticalLineTo(5.25f)
                        horizontalLineToRelative(1.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineTo(5.25f)
                        close()
                        moveToRelative(0f, 12f)
                        verticalLineToRelative(-1.5f)
                        horizontalLineToRelative(1.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineTo(5.25f)
                        close()
                        moveToRelative(12f, -12f)
                        verticalLineTo(5.25f)
                        horizontalLineToRelative(1.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineToRelative(-1.5f)
                        close()
                    }
                }
                .build()
        return qrCode2!!
    }

private var qrCode2: ImageVector? = null
