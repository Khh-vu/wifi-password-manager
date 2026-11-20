package io.github.wifi_password_manager.domain.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface WifiConnectionStatus {
    data object Disconnected : WifiConnectionStatus

    data class Connecting(val ssid: String, val password: String) : WifiConnectionStatus {
        fun matches(network: WifiNetwork): Boolean =
            ssid == network.ssid && password == network.password
    }

    data class Connected(val ssid: String, val password: String) : WifiConnectionStatus {
        fun matches(network: WifiNetwork): Boolean =
            ssid == network.ssid && password == network.password && network.existInSystem
    }
}
