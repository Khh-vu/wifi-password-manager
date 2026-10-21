package io.github.wifi_password_manager.ipc

import android.net.MacAddress
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfigurationHidden
import android.os.Parcelable
import dev.rikka.tools.refine.Refine
import io.github.wifi_password_manager.utils.simpleKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class WifiNetworkParcel(
    val networkId: Int,
    val ssid: String,
    val password: String,
    val securityType: Int,
    val hidden: Boolean,
    val autojoin: Boolean,
    val shared: Boolean,
    val meteredOverride: Int,
    val macRandomizationSetting: Int,
    val randomizedMacAddress: MacAddress,
) : Parcelable {
    fun toWifiConfiguration(): WifiConfiguration {
        val config =
            WifiConfigurationHidden().apply {
                networkId = this@WifiNetworkParcel.networkId
                SSID = "\"$ssid\""
                hiddenSSID = hidden
                allowAutojoin = autojoin
                shared = this@WifiNetworkParcel.shared
                meteredOverride = this@WifiNetworkParcel.meteredOverride
                macRandomizationSetting = this@WifiNetworkParcel.macRandomizationSetting

                when (this@WifiNetworkParcel.securityType) {
                    WifiConfiguration.SECURITY_TYPE_OPEN -> {
                        setSecurityParams(WifiConfiguration.SECURITY_TYPE_OPEN)
                    }

                    WifiConfiguration.SECURITY_TYPE_OWE -> {
                        setSecurityParams(WifiConfiguration.SECURITY_TYPE_OWE)
                    }

                    WifiConfiguration.SECURITY_TYPE_PSK -> {
                        setSecurityParams(WifiConfiguration.SECURITY_TYPE_PSK)
                        preSharedKey = "\"$password\""
                    }

                    WifiConfiguration.SECURITY_TYPE_SAE -> {
                        setSecurityParams(WifiConfiguration.SECURITY_TYPE_SAE)
                        preSharedKey = "\"$password\""
                    }

                    WifiConfiguration.SECURITY_TYPE_WEP -> {
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
            }
        return Refine.unsafeCast(config)
    }

    companion object {
        fun fromWifiConfiguration(config: WifiConfiguration): WifiNetworkParcel {
            val hidden = Refine.unsafeCast<WifiConfigurationHidden>(config)
            return WifiNetworkParcel(
                networkId = hidden.networkId,
                ssid = hidden.printableSsid,
                password = hidden.simpleKey,
                securityType = getSecurityType(hidden),
                hidden = hidden.hiddenSSID,
                autojoin = hidden.allowAutojoin,
                shared = hidden.shared,
                meteredOverride = hidden.meteredOverride,
                macRandomizationSetting = hidden.macRandomizationSetting,
                randomizedMacAddress = hidden.randomizedMacAddress,
            )
        }

        private fun getSecurityType(config: WifiConfigurationHidden): Int {
            val keyMgmt = config.allowedKeyManagement
            return when {
                keyMgmt.get(WifiConfiguration.KeyMgmt.SAE) -> WifiConfiguration.SECURITY_TYPE_SAE
                keyMgmt.get(WifiConfiguration.KeyMgmt.OWE) -> WifiConfiguration.SECURITY_TYPE_OWE
                keyMgmt.get(WifiConfiguration.KeyMgmt.WPA_PSK) ||
                    keyMgmt.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                    keyMgmt.get(WifiConfiguration.KeyMgmt.WPA2_PSK) ->
                    WifiConfiguration.SECURITY_TYPE_PSK

                keyMgmt.get(WifiConfiguration.KeyMgmt.NONE) && config.wepKeys[0] != null ->
                    WifiConfiguration.SECURITY_TYPE_WEP
                else -> WifiConfiguration.SECURITY_TYPE_OPEN
            }
        }
    }
}
