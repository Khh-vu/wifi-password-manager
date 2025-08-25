package io.github.wifi_password_manager.ui.screen.main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.QrData
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import io.github.alexzhirkevich.qrose.wifi
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.data.WifiNetwork
import io.github.wifi_password_manager.data.WifiNetwork.SecurityType
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import io.github.wifi_password_manager.utils.MOCK

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiQRDialog(
    modifier: Modifier = Modifier,
    network: WifiNetwork,
    onDismiss: () -> Unit,
    backgroundColor: Color = AlertDialogDefaults.containerColor,
    foregroundColor: Color = AlertDialogDefaults.iconContentColor,
) {
    val wifiData =
        QrData.wifi(
            authentication =
                when {
                    SecurityType.WEP in network.securityType -> "WEP"
                    SecurityType.WPA2 in network.securityType ||
                        SecurityType.WPA3 in network.securityType -> "WPA"
                    else -> null
                },
            ssid = network.ssid,
            psk = network.password.takeIf { it.isNotBlank() },
            hidden = network.hidden,
        )
    val wifiCode =
        rememberQrCodePainter(wifiData) {
            errorCorrectionLevel = QrErrorCorrectionLevel.Medium
            colors { dark = QrBrush.solid(foregroundColor) }
        }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            color = backgroundColor,
            shape = AlertDialogDefaults.shape,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            TooltipBox(
                positionProvider =
                    TooltipDefaults.rememberTooltipPositionProvider(
                        positioning = TooltipAnchorPosition.Below
                    ),
                tooltip = { PlainTooltip { Text(text = wifiData) } },
                state = rememberTooltipState(),
            ) {
                Image(
                    painter = wifiCode,
                    contentDescription = stringResource(R.string.wifi_qr_code),
                    modifier = modifier.aspectRatio(1f).padding(32.dp),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun WifiQRDialogPreview() {
    WiFiPasswordManagerTheme { WifiQRDialog(network = WifiNetwork.MOCK.random(), onDismiss = {}) }
}
