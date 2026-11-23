package io.github.wifi_password_manager.data.datasource.wifi

import android.annotation.SuppressLint
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfigurationHidden
import android.os.Build
import dev.rikka.tools.refine.Refine
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.model.WifiNetwork.SecurityType

val WifiConfigurationHidden.simpleKey: String
    get() {
        return when {
            !preSharedKey.isNullOrBlank() -> preSharedKey.removeSurrounding("\"")
            !wepKeys.all { it.isNullOrBlank() } ->
                wepKeys.filterNotNull().joinToString("\n") { it.removeSurrounding("\"") }

            else -> ""
        }
    }

val WifiConfigurationHidden.securityType: SecurityType
    get() {
        return when {
            allowedKeyManagement.get(WifiConfiguration.KeyMgmt.SAE) -> SecurityType.WPA3

            allowedKeyManagement.get(WifiConfiguration.KeyMgmt.OWE) -> SecurityType.OWE

            allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK) ||
                allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA2_PSK) -> SecurityType.WPA2

            allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE) && wepKeys[0] != null ->
                SecurityType.WEP

            else -> SecurityType.OPEN
        }
    }

fun WifiNetwork.Companion.fromWifiConfiguration(config: WifiConfiguration): WifiNetwork {
    val network = Refine.unsafeCast<WifiConfigurationHidden>(config)
    return WifiNetwork(
        networkId = network.networkId,
        ssid = network.printableSsid,
        securityType = setOf(network.securityType),
        password = network.simpleKey,
        hidden = network.hiddenSSID,
        autojoin = Build.VERSION.SDK_INT < Build.VERSION_CODES.R || network.allowAutojoin,
        private = !network.shared,
        meteredOverride = network.meteredOverride,
        macRandomizationSetting = network.macRandomizationSetting,
        randomizedMacAddress =
            network.randomizedMacAddress.toString().takeIf { it != "02:00:00:00:00:00" },
    )
}

@SuppressLint("InlinedApi")
fun WifiNetwork.toWifiConfigurations(): List<WifiConfiguration> {
    return securityType.map { type ->
        val config =
            WifiConfigurationHidden().apply {
                networkId = if (existInSystem) this@toWifiConfigurations.networkId else -1
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    allowAutojoin = autojoin
                }
                hiddenSSID = hidden
                shared = !private
                meteredOverride = this@toWifiConfigurations.meteredOverride
                macRandomizationSetting =
                    this@toWifiConfigurations.macRandomizationSetting.let {
                        if (
                            Build.VERSION.SDK_INT < Build.VERSION_CODES.S &&
                                (it == WifiNetwork.Randomization.NON_PERSISTENT.value ||
                                    it == WifiNetwork.Randomization.AUTO.value)
                        ) {
                            WifiNetwork.Randomization.PERSISTENT.value
                        } else {
                            it
                        }
                    }
            }
        Refine.unsafeCast(config)
    }
}
