package io.github.wifi_password_manager.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
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
import io.github.wifi_password_manager.utils.awaitAction
import io.github.wifi_password_manager.utils.fromWifiConfiguration
import io.github.wifi_password_manager.utils.toWifiConfigurations
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch

class WifiRepositoryImpl(
    private val context: Context,
    private val wifiNetworkDao: WifiNetworkDao,
    private val privilegedManager: PrivilegedManager,
    private val dispatcher: CoroutineDispatcher,
) : WifiRepository {
    companion object {
        private const val TAG = "WifiRepository"
    }

    private val connectivityManager by lazy { context.getSystemService<ConnectivityManager>() }

    private var cachedPrivilegedMode: PrivilegedMode? = null
    private var cachedDataSource: WifiDataSource? = null

    private val dataSource: WifiDataSource
        get() {
            val mode = privilegedManager.currentMode
            if (mode != cachedPrivilegedMode) {
                cachedPrivilegedMode = mode
                cachedDataSource = createDataSource(mode)
            }
            return cachedDataSource ?: createDataSource(mode).also { cachedDataSource = it }
        }

    private fun createDataSource(mode: PrivilegedMode): WifiDataSource {
        return when (mode) {
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
            wifiNetworkDao.markAllNotExistInSystem()
            return@dispatcher
        }

        val existingNetworks = wifiNetworkDao.getAllNetworksList()
        val networksWithNotes = networks.map { network ->
            val existingNote = existingNetworks.firstOrNull { it.ssid == network.ssid }?.note
            network.copy(note = existingNote ?: network.note).toEntity()
        }
        wifiNetworkDao.upsertNetworks(networksWithNotes)

        val systemSsids = networks.map { it.ssid }
        wifiNetworkDao.markNotExistInSystem(systemSsids)
    }

    private suspend fun connectInternal(config: WifiConfiguration): Boolean {
        return awaitAction { listener -> dataSource.connect(config, listener) }
    }

    override fun getConnectedWifiSsidFlow(): Flow<String> {
        return callbackFlow {
            suspend fun sendInfo(wifiInfo: WifiInfo?) {
                val ssid = wifiInfo?.ssid?.takeIf { it != WifiManager.UNKNOWN_SSID }
                    ?.removeSurrounding("\"").orEmpty()
                send(ssid)
                refresh()
            }

            val callback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                    override fun onCapabilitiesChanged(
                        network: Network,
                        capabilities: NetworkCapabilities,
                    ) {
                        launch {
                            sendInfo(
                                dataSource.getConnectionInfo()
                                    ?: capabilities.transportInfo as? WifiInfo
                            )
                        }
                    }

                    override fun onLost(network: Network) {
                        launch { sendInfo(null) }
                    }
                }
            } else {
                object : ConnectivityManager.NetworkCallback() {
                    override fun onCapabilitiesChanged(
                        network: Network,
                        capabilities: NetworkCapabilities,
                    ) {
                        launch {
                            sendInfo(
                                dataSource.getConnectionInfo()
                                    ?: capabilities.transportInfo as? WifiInfo
                            )
                        }
                    }

                    override fun onLost(network: Network) {
                        launch { sendInfo(null) }
                    }
                }
            }

            val request =
                NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build()

            sendInfo(dataSource.getConnectionInfo())

            connectivityManager?.registerNetworkCallback(request, callback)
            awaitClose { connectivityManager?.unregisterNetworkCallback(callback) }
        }.distinctUntilChanged()
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
        if (privilegedManager.currentMode == PrivilegedMode.NONE) return
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

    override suspend fun forget(netId: Int): Boolean {
        return dataSource.forget(netId)
    }

    override suspend fun delete(ssid: String) {
        wifiNetworkDao.deleteNetwork(ssid)
    }

    override suspend fun persistEphemeralNetworks() {
        dataSource.persistEphemeralNetworks()
    }

    override suspend fun updateNote(ssid: String, note: String?) {
        wifiNetworkDao.updateNote(ssid, note)
    }

    override suspend fun disconnect(): Boolean {
        return dataSource.disconnect()
    }

    override suspend fun connect(network: WifiNetwork) {
        for (config in network.toWifiConfigurations()) {
            if (connectInternal(config)) return
        }
    }
}
