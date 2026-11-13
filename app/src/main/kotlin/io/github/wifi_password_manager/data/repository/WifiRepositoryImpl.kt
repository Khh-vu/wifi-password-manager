package io.github.wifi_password_manager.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.core.content.getSystemService
import io.github.wifi_password_manager.data.datasource.wifi.CachedWifiDataSourceImpl
import io.github.wifi_password_manager.data.datasource.wifi.RootWifiDataSourceImpl
import io.github.wifi_password_manager.data.datasource.wifi.ShizukuUserServiceWifiDataSourceImpl
import io.github.wifi_password_manager.data.datasource.wifi.ShizukuWifiDataSourceImpl
import io.github.wifi_password_manager.data.datasource.wifi.WifiDataSource
import io.github.wifi_password_manager.data.local.dao.WifiNetworkDao
import io.github.wifi_password_manager.data.local.entity.toDomain
import io.github.wifi_password_manager.data.local.entity.toEntity
import io.github.wifi_password_manager.domain.model.PrivilegedMode
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.manager.PrivilegedManager
import io.github.wifi_password_manager.manager.SystemApiErrorReporter
import io.github.wifi_password_manager.manager.isSystemApiMismatch
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
import rikka.shizuku.Shizuku

class WifiRepositoryImpl(
    private val context: Context,
    private val wifiNetworkDao: WifiNetworkDao,
    private val privilegedManager: PrivilegedManager,
    private val systemApiErrorReporter: SystemApiErrorReporter,
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
                if (
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.Q &&
                        Shizuku.getUid() == Process.ROOT_UID
                ) {
                    Log.d(TAG, "Using ShizukuUserServiceWifiDataSource")
                    ShizukuUserServiceWifiDataSourceImpl(context)
                } else {
                    Log.d(TAG, "Using ShizukuWifiDataSource")
                    ShizukuWifiDataSourceImpl(context)
                }
            }

            PrivilegedMode.NONE -> {
                Log.d(TAG, "Using CachedWifiDataSource")
                CachedWifiDataSourceImpl(context, wifiNetworkDao)
            }
        }
    }

    private suspend fun <T> withDataSource(default: T, block: suspend WifiDataSource.() -> T): T =
        dispatcher {
            try {
                block(dataSource)
            } catch (e: Throwable) {
                if (!e.isSystemApiMismatch) throw e
                systemApiErrorReporter.report(e)
                default
            }
        }

    private suspend fun getConnectionInfo(): WifiInfo? =
        withDataSource(null) { getConnectionInfo() }

    override fun getConnectedWifiSsidFlow(): Flow<String> {
        return callbackFlow {
                @SuppressLint("InlinedApi")
                suspend fun sendInfo(wifiInfo: WifiInfo?) {
                    val ssid =
                        wifiInfo
                            ?.ssid
                            ?.removeSurrounding("\"")
                            ?.takeIf { it != WifiManager.UNKNOWN_SSID }
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
                                        getConnectionInfo()
                                            ?: capabilities.transportInfo as? WifiInfo,
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
                                        getConnectionInfo()
                                            ?: capabilities.transportInfo as? WifiInfo,
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

                sendInfo(getConnectionInfo())

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

    override suspend fun refresh(): Boolean {
        if (!privilegedManager.currentMode.hasPrivilegedAccess) return true
        val configs = withDataSource(null) { getPrivilegedConfiguredNetworks() } ?: return false
        dispatcher {
            val networks = configs.map(WifiNetwork::fromWifiConfiguration)
            if (networks.isEmpty()) {
                wifiNetworkDao.markAllNotExistInSystem()
                return@dispatcher
            }
            wifiNetworkDao.syncNetworks(networks.map { it.toEntity() })
        }
        return true
    }

    override suspend fun getPrivilegedConfiguredNetworks(): List<WifiConfiguration> =
        withDataSource(emptyList()) { getPrivilegedConfiguredNetworks() }

    override suspend fun addOrUpdateNetworkPrivileged(config: WifiConfiguration): Boolean =
        withDataSource(-1) { addOrUpdateNetworkPrivileged(config) } != -1

    override suspend fun forget(netId: Int): Boolean = withDataSource(false) { forget(netId) }

    override suspend fun delete(ssid: String) {
        wifiNetworkDao.deleteNetwork(ssid)
    }

    override suspend fun clearCache() {
        wifiNetworkDao.clearNetworks()
    }

    override suspend fun persistEphemeralNetworks() =
        withDataSource(Unit) { persistEphemeralNetworks() }

    override suspend fun updateNote(ssid: String, note: String?) {
        wifiNetworkDao.updateNote(ssid, note)
    }

    override suspend fun disconnect(): Boolean = withDataSource(false) { disconnect() }

    override suspend fun connect(network: WifiNetwork) =
        withDataSource(Unit) {
            val networks = getPrivilegedConfiguredNetworks().map(WifiNetwork::fromWifiConfiguration)
            val netIds = networks.filter { it.ssid == network.ssid }.map { it.networkId }

            for (netId in netIds) {
                if (connect(netId = netId)) return@withDataSource
            }

            for (config in network.copy(existInSystem = false).toWifiConfigurations()) {
                if (connect(config = config)) return@withDataSource
            }
        }
}
