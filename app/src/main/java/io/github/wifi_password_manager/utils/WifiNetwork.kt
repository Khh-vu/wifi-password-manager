@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.utils

import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfigurationHidden
import dev.rikka.tools.refine.Refine
import io.github.wifi_password_manager.data.WifiNetwork
import io.github.wifi_password_manager.data.WifiNetwork.SecurityType
import kotlin.random.Random

fun WifiNetwork.Companion.fromWifiConfiguration(config: WifiConfiguration): WifiNetwork {
    val network = Refine.unsafeCast<WifiConfigurationHidden>(config)
    return WifiNetwork(
        networkId = network.networkId,
        ssid = network.SSID.stripQuotes(),
        securityType = setOf(network.securityType),
        password = network.simpleKey,
        hidden = network.hiddenSSID,
        autojoin = network.allowAutojoin,
    )
}

val WifiNetwork.Companion.MOCK
    get() =
        List(20) {
            val type = SecurityType.entries.random()
            WifiNetwork(
                ssid = "ssid $it",
                password =
                    if (type !in setOf(SecurityType.OWE, SecurityType.OPEN)) {
                        "password $it"
                    } else {
                        ""
                    },
                securityType = setOf(type),
                hidden = Random.nextBoolean(),
            )
        }

fun List<WifiNetwork>.groupAndSortedBySsid(): List<WifiNetwork> =
    groupBy { it.ssid }
        .values
        .map { duplicateNetworks ->
            WifiNetwork(
                networkId = duplicateNetworks.first().networkId,
                ssid = duplicateNetworks.first().ssid,
                securityType = duplicateNetworks.flatMap { it.securityType }.toSet(),
                password =
                    duplicateNetworks.firstOrNull { it.password.isNotEmpty() }?.password ?: "",
                hidden = duplicateNetworks.any { it.hidden },
                autojoin = duplicateNetworks.all { it.autojoin },
            )
        }
        .sortedBy { it.ssid.lowercase() }

fun WifiNetwork.toWifiConfigurations(): List<WifiConfiguration> {
    return securityType.map { type ->
        val config =
            WifiConfigurationHidden().apply {
                SSID = "\"$ssid\""
                when (type) {
                    SecurityType.OPEN -> {
                        setSecurityParams(WifiConfiguration.SECURITY_TYPE_OPEN)
                    }

                    SecurityType.OWE -> {
                        setSecurityParams(WifiConfiguration.SECURITY_TYPE_OWE)
                    }

                    SecurityType.WPA2 -> {
                        setSecurityParams(WifiConfiguration.SECURITY_TYPE_PSK)
                        preSharedKey = "\"$password\""
                    }

                    SecurityType.WPA3 -> {
                        setSecurityParams(WifiConfiguration.SECURITY_TYPE_SAE)
                        preSharedKey = "\"$password\""
                    }

                    SecurityType.WEP -> {
                        setSecurityParams(WifiConfiguration.SECURITY_TYPE_WEP)
                        // WEP-40, WEP-104, and WEP-256
                        if (
                            (password.length == 10 ||
                                password.length == 26 ||
                                password.length == 58) && password matches "[0-9A-Fa-f]*".toRegex()
                        ) {
                            wepKeys[0] = password
                        } else {
                            wepKeys[0] = "\"$password\""
                        }
                    }
                }
                allowAutojoin = autojoin
                hiddenSSID = hidden
            }
        Refine.unsafeCast(config)
    }
}
