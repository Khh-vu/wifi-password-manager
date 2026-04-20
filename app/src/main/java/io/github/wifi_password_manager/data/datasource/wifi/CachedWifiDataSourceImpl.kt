@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.data.datasource.wifi

import android.net.wifi.WifiConfiguration
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

    override suspend fun removeNetwork(netId: Int): Boolean {
        return false
    }

    override suspend fun persistEphemeralNetworks() {}
}
