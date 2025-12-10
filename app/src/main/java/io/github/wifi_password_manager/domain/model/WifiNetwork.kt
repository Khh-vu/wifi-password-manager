package io.github.wifi_password_manager.domain.model

import io.github.wifi_password_manager.utils.ImmutableSetSerializer
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class WifiNetwork(
    @Transient val networkId: Int = -1,
    val ssid: String,
    @SerialName("security")
    @Serializable(with = ImmutableSetSerializer::class)
    val securityType: ImmutableSet<SecurityType>,
    val password: String,
    val hidden: Boolean = false,
    val autojoin: Boolean = true,
    val private: Boolean = false,
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
