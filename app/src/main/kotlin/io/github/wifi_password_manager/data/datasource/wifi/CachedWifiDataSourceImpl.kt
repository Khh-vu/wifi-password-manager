package io.github.wifi_password_manager.data.datasource.wifi

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.getSystemService
import io.github.wifi_password_manager.data.local.dao.WifiNetworkDao
import io.github.wifi_password_manager.data.local.entity.toDomain
import io.github.wifi_password_manager.utils.toWifiConfigurations

class CachedWifiDataSourceImpl(
    private val context: Context,
    private val wifiNetworkDao: WifiNetworkDao,
) : WifiDataSource {
    private val wifiManager by lazy { context.getSystemService<WifiManager>() }

    override suspend fun getPrivilegedConfiguredNetworks(): List<WifiConfiguration> {
        val networks = wifiNetworkDao.getAllNetworksList().map { it.toDomain() }
        return networks.flatMap { it.toWifiConfigurations() }
    }

    override suspend fun addOrUpdateNetworkPrivileged(config: WifiConfiguration): Int {
        return -1
    }

    override suspend fun forget(netId: Int): Boolean {
        return false
    }

    @Suppress("DEPRECATION")
    override suspend fun getConnectionInfo(): WifiInfo? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return null
        return wifiManager?.connectionInfo
    }

    override suspend fun persistEphemeralNetworks() {}

    override suspend fun disconnect(): Boolean {
        return false
    }

    override suspend fun connect(config: WifiConfiguration?, netId: Int): Boolean {
        return false
    }
}
