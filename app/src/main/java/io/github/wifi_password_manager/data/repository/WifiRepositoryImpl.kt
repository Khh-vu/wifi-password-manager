@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.data.repository

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.util.Log
import io.github.wifi_password_manager.data.datasource.wifi.CachedWifiDataSourceImpl
import io.github.wifi_password_manager.data.datasource.wifi.RootWifiDataSourceImpl
import io.github.wifi_password_manager.data.datasource.wifi.ShizukuWifiDataSourceImpl
import io.github.wifi_password_manager.data.datasource.wifi.WifiDataSource
import io.github.wifi_password_manager.data.local.dao.WifiNetworkDao
import io.github.wifi_password_manager.data.local.entity.toDomain
import io.github.wifi_password_manager.data.local.entity.toEntity
import io.github.wifi_password_manager.domain.model.PrivilegedMode
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.manager.PrivilegedManager
import io.github.wifi_password_manager.utils.fromWifiConfiguration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.invoke

class WifiRepositoryImpl(
    private val context: Context,
    private val wifiNetworkDao: WifiNetworkDao,
    private val privilegedManager: PrivilegedManager,
    private val dispatcher: CoroutineDispatcher,
) : WifiRepository {
    companion object {
        private const val TAG = "WifiRepository"
    }

    private val dataSource: WifiDataSource by lazy {
        val mode = privilegedManager.currentMode
        when (mode) {
            PrivilegedMode.ROOT -> {
                Log.d(TAG, "Using RootWifiDataSource")
                RootWifiDataSourceImpl(context)
            }
            PrivilegedMode.SHIZUKU -> {
                Log.d(TAG, "Using ShizukuWifiDataSource")
                ShizukuWifiDataSourceImpl(context)
            }
            PrivilegedMode.NONE -> {
                Log.d(TAG, "Using CachedWifiDataSource")
                CachedWifiDataSourceImpl(wifiNetworkDao)
            }
        }
    }

    private suspend fun syncNetworksToDatabase(networks: List<WifiNetwork>) = dispatcher {
        if (networks.isEmpty()) {
            wifiNetworkDao.deleteNetworks()
            return@dispatcher
        }

        val existingNetworks = wifiNetworkDao.getAllNetworksList()
        val networksWithNotes = networks.map { network ->
            val existingNote = existingNetworks.firstOrNull { it.ssid == network.ssid }?.note
            network.copy(note = existingNote ?: network.note).toEntity()
        }
        wifiNetworkDao.upsertNetworks(networksWithNotes)

        val systemSsids = networks.map { it.ssid }
        wifiNetworkDao.deleteNetworks(systemSsids)
    }

    override fun getAllNetworks(): Flow<List<WifiNetwork>> =
        wifiNetworkDao.getAllNetworks().map { entities -> entities.map { it.toDomain() } }

    override fun getAllNetworks(query: String): Flow<List<WifiNetwork>> =
        wifiNetworkDao.getAllNetworks(query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getAllNetworksList(): List<WifiNetwork> {
        return wifiNetworkDao.getAllNetworksList().map { it.toDomain() }
    }

    override suspend fun getNetworkCount(): Int {
        return wifiNetworkDao.getNetworkCount()
    }

    override suspend fun refresh() {
        val configs = getPrivilegedConfiguredNetworks()
        val networks = configs.map(WifiNetwork::fromWifiConfiguration)
        syncNetworksToDatabase(networks)
    }

    override suspend fun getPrivilegedConfiguredNetworks(): List<WifiConfiguration> {
        return dataSource.getPrivilegedConfiguredNetworks()
    }

    override suspend fun addOrUpdateNetworkPrivileged(config: WifiConfiguration): Boolean {
        return dataSource.addOrUpdateNetworkPrivileged(config)
    }

    override suspend fun removeNetwork(netId: Int): Boolean {
        return dataSource.removeNetwork(netId)
    }

    override suspend fun persistEphemeralNetworks() {
        dataSource.persistEphemeralNetworks()
    }

    override suspend fun updateNote(ssid: String, note: String?) {
        wifiNetworkDao.updateNote(ssid, note)
    }
}
