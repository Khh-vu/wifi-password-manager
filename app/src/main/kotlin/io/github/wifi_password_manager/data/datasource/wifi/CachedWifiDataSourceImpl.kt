package io.github.wifi_password_manager.data.datasource.wifi

import android.net.wifi.IActionListener
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import io.github.wifi_password_manager.data.local.dao.WifiNetworkDao
import io.github.wifi_password_manager.data.local.entity.toDomain
import io.github.wifi_password_manager.utils.toWifiConfigurations

class CachedWifiDataSourceImpl(private val wifiNetworkDao: WifiNetworkDao) : WifiDataSource {
    override suspend fun getPrivilegedConfiguredNetworks(): List<WifiConfiguration> {
        val networks = wifiNetworkDao.getAllNetworksList().map { it.toDomain() }
        return networks.flatMap { it.toWifiConfigurations() }
    }

    override suspend fun addOrUpdateNetworkPrivileged(config: WifiConfiguration): Boolean {
        return false
    }

    override suspend fun forget(netId: Int): Boolean {
        return false
    }

    override suspend fun getConnectionInfo(): WifiInfo? {
        return null
    }

    override suspend fun persistEphemeralNetworks() {}

    override suspend fun disconnect(): Boolean {
        return false
    }

    override suspend fun connect(config: WifiConfiguration, listener: IActionListener) {
        listener.onFailure(0)
    }
}
