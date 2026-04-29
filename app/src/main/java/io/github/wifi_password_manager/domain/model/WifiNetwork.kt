package io.github.wifi_password_manager.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Immutable
@Serializable
data class WifiNetwork(
    @Transient val networkId: Int = -1,
    val ssid: String,
    @SerialName("security")
    val securityType: Set<SecurityType>,
    val password: String,
    val hidden: Boolean = false,
    val autojoin: Boolean = true,
    val private: Boolean = false,
    val note: String? = null,
) {
    @Serializable
    enum class SecurityType {
        OPEN,
        OWE,
        WPA2,
        WPA3,
        WEP,
    }
}
