package io.github.wifi_password_manager.domain.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface WifiConnectionStatus {
    data object Disconnected : WifiConnectionStatus

    data class Connecting(val ssid: String) : WifiConnectionStatus

    data class Connected(val ssid: String) : WifiConnectionStatus
}
