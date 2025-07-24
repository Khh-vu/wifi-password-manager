@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.data

import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfigurationHidden
import dev.rikka.tools.refine.Refine
import io.github.wifi_password_manager.utils.securityType
import io.github.wifi_password_manager.utils.simpleKey
import io.github.wifi_password_manager.utils.stripQuotes
import kotlin.random.Random
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class WifiNetwork(
    @Transient val key: String = "",
    @Transient val networkId: Int = -1,
    val ssid: String,
    @SerialName("security") val securityType: SecurityType,
    val password: String,
    val hidden: Boolean = false,
    val autojoin: Boolean = true,
) {
    companion object {
        fun fromWifiConfiguration(config: WifiConfiguration): WifiNetwork {
            val network = Refine.unsafeCast<WifiConfigurationHidden>(config)
            return WifiNetwork(
                key = network.key,
                networkId = network.networkId,
                ssid = network.SSID.stripQuotes(),
                securityType = network.securityType,
                password = network.simpleKey,
                hidden = network.hiddenSSID,
                autojoin = network.allowAutojoin,
            )
        }

        val MOCK =
            (1..100).shuffled().map {
                val type = SecurityType.entries.random()
                WifiNetwork(
                    key = "$it",
                    ssid = "ssid $it",
                    password =
                        if (type !in setOf(SecurityType.OWE, SecurityType.OPEN)) {
                            "password $it"
                        } else {
                            ""
                        },
                    securityType = type,
                    hidden = Random.nextBoolean(),
                )
            }
    }

    @Serializable
    enum class SecurityType {
        OPEN,
        OWE,
        WPA2,
        WPA3,
        WEP,
    }
}
