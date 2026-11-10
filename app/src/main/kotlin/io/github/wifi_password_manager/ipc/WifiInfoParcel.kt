package io.github.wifi_password_manager.ipc

import android.net.wifi.WifiInfo
import android.net.wifi.WifiInfoHidden
import android.net.wifi.WifiSsidHidden
import android.os.Build
import android.os.Parcelable
import dev.rikka.tools.refine.Refine
import kotlinx.parcelize.Parcelize

@Parcelize
data class WifiInfoParcel(
    val ssid: String,
    val rssi: Int,
) : Parcelable {
    fun toWifiInfo(): WifiInfo {
        val ssidBytes = ssid.removeSurrounding("\"").toByteArray()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WifiInfo.Builder()
                .apply {
                    setSsid(ssidBytes)
                    setRssi(rssi)
                }
                .build()
        } else {
            Refine.unsafeCast(
                WifiInfoHidden().apply {
                    setSSID(WifiSsidHidden.createFromByteArray(ssidBytes))
                    setRssi(rssi)
                },
            )
        }
    }

    companion object {
        fun fromWifiInfo(wifiInfo: WifiInfo): WifiInfoParcel {
            return WifiInfoParcel(wifiInfo.ssid, wifiInfo.rssi)
        }
    }
}
