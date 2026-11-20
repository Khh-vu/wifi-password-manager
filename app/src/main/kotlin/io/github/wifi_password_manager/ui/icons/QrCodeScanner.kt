package io.github.wifi_password_manager.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QrCodeScanner: ImageVector
    get() {
        if (qrCodeScanner != null) {
            return qrCodeScanner!!
        }
        qrCodeScanner =
            ImageVector.Builder(
                    name = "qr_code_scanner",
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
                        moveTo(2f, 7f)
                        verticalLineToRelative(-5f)
                        horizontalLineToRelative(5f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(4f)
                        verticalLineToRelative(3f)
                        horizontalLineTo(2f)
                        close()
                        moveToRelative(0f, 15f)
                        verticalLineToRelative(-5f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(3f)
                        horizontalLineToRelative(3f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(2f)
                        close()
                        moveToRelative(15f, 0f)
                        verticalLineToRelative(-2f)
                        horizontalLineToRelative(3f)
                        verticalLineToRelative(-3f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(5f)
                        horizontalLineTo(17f)
                        close()
                        moveToRelative(3f, -15f)
                        verticalLineToRelative(-3f)
                        horizontalLineTo(17f)
                        verticalLineToRelative(-2f)
                        horizontalLineToRelative(5f)
                        verticalLineToRelative(5f)
                        horizontalLineToRelative(-2f)
                        close()
                        moveTo(17.5f, 17.5f)
                        horizontalLineToRelative(1.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineToRelative(-1.5f)
                        verticalLineToRelative(-1.5f)
                        close()
                        moveToRelative(0f, -3f)
                        horizontalLineToRelative(1.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineToRelative(-1.5f)
                        verticalLineToRelative(-1.5f)
                        close()
                        moveToRelative(-1.5f, 1.5f)
                        horizontalLineToRelative(1.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineToRelative(-1.5f)
                        verticalLineToRelative(-1.5f)
                        close()
                        moveToRelative(-1.5f, 1.5f)
                        horizontalLineToRelative(1.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineToRelative(-1.5f)
                        verticalLineToRelative(-1.5f)
                        close()
                        moveToRelative(-1.5f, -1.5f)
                        horizontalLineToRelative(1.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineToRelative(-1.5f)
                        verticalLineToRelative(-1.5f)
                        close()
                        moveToRelative(3f, -3f)
                        horizontalLineToRelative(1.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineToRelative(-1.5f)
                        verticalLineToRelative(-1.5f)
                        close()
                        moveToRelative(-1.5f, 1.5f)
                        horizontalLineToRelative(1.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineToRelative(-1.5f)
                        verticalLineToRelative(-1.5f)
                        close()
                        moveToRelative(-1.5f, -1.5f)
                        horizontalLineToRelative(1.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineToRelative(-1.5f)
                        verticalLineToRelative(-1.5f)
                        close()
                        moveToRelative(6f, -8f)
                        verticalLineToRelative(6f)
                        horizontalLineTo(13f)
                        verticalLineToRelative(-6f)
                        horizontalLineToRelative(6f)
                        close()
                        moveTo(11f, 13f)
                        verticalLineToRelative(6f)
                        horizontalLineTo(5f)
                        verticalLineToRelative(-6f)
                        horizontalLineToRelative(6f)
                        close()
                        moveToRelative(0f, -8f)
                        verticalLineToRelative(6f)
                        horizontalLineTo(5f)
                        verticalLineToRelative(-6f)
                        horizontalLineToRelative(6f)
                        close()
                        moveToRelative(-1.5f, 12.5f)
                        verticalLineToRelative(-3f)
                        horizontalLineTo(6.5f)
                        verticalLineToRelative(3f)
                        horizontalLineToRelative(3f)
                        close()
                        moveToRelative(0f, -8f)
                        verticalLineToRelative(-3f)
                        horizontalLineTo(6.5f)
                        verticalLineToRelative(3f)
                        horizontalLineToRelative(3f)
                        close()
                        moveToRelative(8f, 0f)
                        verticalLineToRelative(-3f)
                        horizontalLineTo(14.5f)
                        verticalLineToRelative(3f)
                        horizontalLineToRelative(3f)
                        close()
                    }
                }
                .build()
        return qrCodeScanner!!
    }

private var qrCodeScanner: ImageVector? = null
