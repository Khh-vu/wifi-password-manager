package io.github.wifi_password_manager.ipc

import android.net.wifi.WifiInfo
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WifiInfoParcel(
    val ssid: String,
    val rssi: Int,
) : Parcelable {
    fun toWifiInfo(): WifiInfo {
        return WifiInfo.Builder()
            .apply {
                setSsid(this@WifiInfoParcel.ssid.removeSurrounding("\"").toByteArray())
                setRssi(this@WifiInfoParcel.rssi)
            }
            .build()
    }

    companion object {
        fun fromWifiInfo(wifiInfo: WifiInfo): WifiInfoParcel {
            return WifiInfoParcel(wifiInfo.ssid, wifiInfo.rssi)
        }
    }
}
