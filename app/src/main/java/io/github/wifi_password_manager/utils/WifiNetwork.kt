@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.utils

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfigurationHidden
import android.os.Build
import android.os.PersistableBundle
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.toClipEntry
import dev.rikka.tools.refine.Refine
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.model.WifiNetwork.SecurityType
import kotlin.random.Random
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

fun WifiNetwork.Companion.fromWifiConfiguration(config: WifiConfiguration): WifiNetwork {
    val network = Refine.unsafeCast<WifiConfigurationHidden>(config)
    return WifiNetwork(
        networkId = network.networkId,
        ssid = network.printableSsid,
        securityType = persistentSetOf(network.securityType),
        password = network.simpleKey,
        hidden = network.hiddenSSID,
        autojoin = network.allowAutojoin,
        private = !network.shared,
    )
}

val WifiNetwork.Companion.MOCK
    get() =
        List(20) {
                val type = SecurityType.entries.random()
                WifiNetwork(
                    networkId = it,
                    ssid = "ssid $it",
                    password =
                        if (type !in setOf(SecurityType.OWE, SecurityType.OPEN)) {
                            "password $it"
                        } else {
                            ""
                        },
                    securityType = persistentSetOf(type),
                    hidden = Random.nextBoolean(),
                    autojoin = Random.nextBoolean(),
                    private = Random.nextBoolean(),
                )
            }
            .toImmutableList()

fun List<WifiNetwork>.groupAndSortedBySsid(): List<WifiNetwork> =
    groupBy { it.ssid }
        .values
        .map { duplicateNetworks ->
            duplicateNetworks
                .first()
                .copy(securityType = duplicateNetworks.flatMap { it.securityType }.toImmutableSet())
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
                        wepTxKeyIndex = 0
                    }
                }
                allowAutojoin = autojoin
                hiddenSSID = hidden
                shared = !private
            }
        Refine.unsafeCast(config)
    }
}

fun WifiNetwork.getSecurity(context: Context): String =
    securityType.joinToString("/") {
        context.getString(
            when (it) {
                SecurityType.OPEN -> R.string.security_open
                SecurityType.OWE -> R.string.security_owe
                SecurityType.WPA2 -> R.string.security_wpa2
                SecurityType.WPA3 -> R.string.security_wpa3
                SecurityType.WEP -> R.string.security_wep
            }
        )
    }

fun WifiNetwork.passwordClipEntry(isSensitive: Boolean = true): ClipEntry {
    val clipData = ClipData.newPlainText(ssid, password)

    if (isSensitive) {
        clipData.description.extras =
            PersistableBundle().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                } else {
                    putBoolean("android.content.extra.IS_SENSITIVE", true)
                }
            }
    }

    return clipData.toClipEntry()
}
