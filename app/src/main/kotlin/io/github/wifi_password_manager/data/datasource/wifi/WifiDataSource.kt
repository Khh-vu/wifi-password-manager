package io.github.wifi_password_manager.data.datasource.wifi

import android.net.wifi.IActionListener
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo

interface WifiDataSource {
    suspend fun getPrivilegedConfiguredNetworks(): List<WifiConfiguration>

    suspend fun addOrUpdateNetworkPrivileged(config: WifiConfiguration): Boolean

    suspend fun forget(netId: Int): Boolean

    suspend fun getConnectionInfo(): WifiInfo?

    suspend fun persistEphemeralNetworks()

    suspend fun disconnect(): Boolean

    suspend fun connect(config: WifiConfiguration, listener: IActionListener)
}
