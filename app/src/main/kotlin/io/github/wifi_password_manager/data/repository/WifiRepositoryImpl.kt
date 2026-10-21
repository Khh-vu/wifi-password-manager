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
import kotlinx.coroutines.flow.flowOn
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
        @Synchronized
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

    override fun getConnectedWifiSsidFlow(): Flow<String> {
        return callbackFlow {
                suspend fun sendInfo(wifiInfo: WifiInfo?) {
                    val ssid =
                        wifiInfo
                            ?.ssid
                            ?.takeIf { it != WifiManager.UNKNOWN_SSID }
                            ?.removeSurrounding("\"")
                            .orEmpty()
                    send(ssid)
                }

                val callback =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
                    NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .build()

                sendInfo(dataSource.getConnectionInfo())

                connectivityManager?.registerNetworkCallback(request, callback)
                awaitClose { connectivityManager?.unregisterNetworkCallback(callback) }
            }
            .flowOn(dispatcher)
            .distinctUntilChanged()
    }

    override fun getAllNetworks(): Flow<List<WifiNetwork>> =
        wifiNetworkDao
            .getAllNetworks()
            .map { entities -> entities.map { it.toDomain() } }
            .distinctUntilChanged()

    override fun getAllNetworks(query: String): Flow<List<WifiNetwork>> =
        wifiNetworkDao
            .getAllNetworks(query)
            .map { entities -> entities.map { it.toDomain() } }
            .distinctUntilChanged()

    override suspend fun getAllNetworksList(): List<WifiNetwork> {
        return wifiNetworkDao.getAllNetworksList().map { it.toDomain() }
    }

    override suspend fun getNetworkCount(): Int {
        return wifiNetworkDao.getNetworkCount()
    }

    override suspend fun refresh() {
        if (privilegedManager.currentMode == PrivilegedMode.NONE) return
        dispatcher {
            val networks = getPrivilegedConfiguredNetworks().map(WifiNetwork::fromWifiConfiguration)
            if (networks.isEmpty()) {
                wifiNetworkDao.markAllNotExistInSystem()
                return@dispatcher
            }
            wifiNetworkDao.syncNetworks(networks.map { it.toEntity() })
        }
    }

    override suspend fun getPrivilegedConfiguredNetworks(): List<WifiConfiguration> = dispatcher {
        dataSource.getPrivilegedConfiguredNetworks()
    }

    override suspend fun addOrUpdateNetworkPrivileged(config: WifiConfiguration): Boolean =
        dispatcher {
            dataSource.addOrUpdateNetworkPrivileged(config)
        }

    override suspend fun forget(netId: Int): Boolean = dispatcher { dataSource.forget(netId) }

    override suspend fun delete(ssid: String) {
        wifiNetworkDao.deleteNetwork(ssid)
    }

    override suspend fun clearCache() {
        wifiNetworkDao.clearNetworks()
    }

    override suspend fun persistEphemeralNetworks() = dispatcher {
        dataSource.persistEphemeralNetworks()
    }

    override suspend fun updateNote(ssid: String, note: String?) {
        wifiNetworkDao.updateNote(ssid, note)
    }

    override suspend fun disconnect(): Boolean = dispatcher { dataSource.disconnect() }

    override suspend fun connect(network: WifiNetwork) = dispatcher {
        val networks = getPrivilegedConfiguredNetworks().map(WifiNetwork::fromWifiConfiguration)
        val netIds = networks.filter { it.ssid == network.ssid }.map { it.networkId }

        if (netIds.isNotEmpty()) {
            for (netId in netIds) {
                val result = awaitAction { listener ->
                    dataSource.connect(netId = netId, listener = listener)
                }
                if (result) return@dispatcher
            }
        }

        for (config in network.copy(existInSystem = false).toWifiConfigurations()) {
            val result = awaitAction { listener ->
                dataSource.connect(config = config, listener = listener)
            }
            if (result) return@dispatcher
        }
    }
}
