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
