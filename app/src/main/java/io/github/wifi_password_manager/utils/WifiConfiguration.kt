@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.utils

import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfigurationHidden
import io.github.wifi_password_manager.domain.model.WifiNetwork

val WifiConfigurationHidden.simpleKey: String
    get() {
        return when {
            !preSharedKey.isNullOrBlank() -> preSharedKey.stripQuotes()
            !wepKeys.all { it.isNullOrBlank() } ->
                wepKeys.filterNotNull().joinToString("\n") { it.stripQuotes() }

            else -> ""
        }
    }

val WifiConfigurationHidden.securityType: WifiNetwork.SecurityType
    get() {
        return when {
            allowedKeyManagement.get(WifiConfiguration.KeyMgmt.SAE) -> WifiNetwork.SecurityType.WPA3

            allowedKeyManagement.get(WifiConfiguration.KeyMgmt.OWE) -> WifiNetwork.SecurityType.OWE

            allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK) ||
                allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA2_PSK) ->
                WifiNetwork.SecurityType.WPA2

            allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE) && wepKeys[0] != null ->
                WifiNetwork.SecurityType.WEP

            else -> WifiNetwork.SecurityType.OPEN
        }
    }
