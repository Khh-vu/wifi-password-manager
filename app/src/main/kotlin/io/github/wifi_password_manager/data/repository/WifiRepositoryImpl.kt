package io.github.wifi_password_manager.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.core.content.getSystemService
import io.github.wifi_password_manager.data.datasource.wifi.CachedWifiDataSourceImpl
import io.github.wifi_password_manager.data.datasource.wifi.RootWifiDataSourceImpl
import io.github.wifi_password_manager.data.datasource.wifi.ShizukuUserServiceWifiDataSourceImpl
import io.github.wifi_password_manager.data.datasource.wifi.ShizukuWifiDataSourceImpl
import io.github.wifi_password_manager.data.datasource.wifi.WifiDataSource
import io.github.wifi_password_manager.data.datasource.wifi.fromWifiConfiguration
import io.github.wifi_password_manager.data.datasource.wifi.toWifiConfigurations
import io.github.wifi_password_manager.data.local.dao.WifiNetworkDao
import io.github.wifi_password_manager.data.local.entity.WifiNetworkEntity
import io.github.wifi_password_manager.data.local.entity.toDomain
import io.github.wifi_password_manager.data.local.entity.toEntity
import io.github.wifi_password_manager.domain.model.ImportResult
import io.github.wifi_password_manager.domain.model.PrivilegedMode
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.manager.PrivilegedManager
import io.github.wifi_password_manager.manager.SystemApiErrorReporter
import io.github.wifi_password_manager.manager.isSystemApiMismatch
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
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

    private suspend fun <T> withDataSource(
        default: T,
        block: suspend CoroutineScope.(WifiDataSource) -> T,
    ): T =
        withContext(dispatcher) {
            try {
                block(dataSource)
            } catch (e: Throwable) {
                if (!e.isSystemApiMismatch) throw e
                systemApiErrorReporter.report(e)
                default
            }
        }

    private suspend fun getConnectionInfo(): WifiInfo? =
        withDataSource(null) { it.getConnectionInfo() }

    private fun replacedNetworks(
        configs: List<WifiConfiguration>,
        network: WifiNetwork,
    ): List<WifiNetwork> {
        val sameSsid =
            configs.map(WifiNetwork::fromWifiConfiguration).filter { it.ssid == network.ssid }
        val netIds =
            sameSsid
                .filter { it.securityType.any(network.securityType::contains) }
                .map { it.networkId }
                .toSet()
        return sameSsid.filter { it.networkId in netIds }
    }

    private suspend fun markNotExistInSystem(networks: List<WifiNetwork>) {
        wifiNetworkDao.markNotExistInSystem(
            networks.flatMap { old -> old.securityType.map { old.ssid to it.name } },
        )
    }

    private fun manualRows(network: WifiNetwork, now: Long): List<WifiNetworkEntity> =
        network.securityType.map {
            network
                .copy(securityType = setOf(it), existInSystem = false)
                .toEntity()
                .copy(createdAt = now, updatedAt = now)
        }

    override fun getConnectedNetworkIdFlow(): Flow<Int> {
        return callbackFlow {
                suspend fun sendInfo(wifiInfo: WifiInfo?) {
                    send(wifiInfo?.networkId ?: -1)
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
        val configs = withDataSource(null) { it.getPrivilegedConfiguredNetworks() } ?: return false
        withContext(dispatcher) {
            val networks = configs.map(WifiNetwork::fromWifiConfiguration)
            if (networks.isEmpty()) {
                wifiNetworkDao.markAllNotExistInSystem()
                return@withContext
            }
            wifiNetworkDao.syncNetworks(networks.map { it.toEntity() })
        }
        return true
    }

    override suspend fun getPrivilegedConfiguredNetworks(): List<WifiConfiguration> =
        withDataSource(emptyList()) { it.getPrivilegedConfiguredNetworks() }

    override suspend fun addOrUpdateNetworkPrivileged(config: WifiConfiguration): Boolean =
        withDataSource(-1) { it.addOrUpdateNetworkPrivileged(config) } != -1

    override suspend fun forget(netId: Int): Boolean = withDataSource(false) { it.forget(netId) }

    override suspend fun forgetNetwork(network: WifiNetwork) {
        getPrivilegedConfiguredNetworks()
            .map(WifiNetwork::fromWifiConfiguration)
            .filter { it.ssid == network.ssid && it.password == network.password }
            .forEach { forget(it.networkId) }
        wifiNetworkDao.deleteNetwork(network.ssid, network.password)
        refresh()
    }

    override suspend fun forgetAll(clearCache: Boolean): Boolean {
        val netIds = getPrivilegedConfiguredNetworks().map { it.networkId }.filter { it != -1 }
        if (netIds.isEmpty() && !clearCache) {
            Log.d(TAG, "No valid networks to remove")
            return false
        }
        coroutineScope { netIds.map { async { forget(it) } }.awaitAll() }
        if (clearCache) wifiNetworkDao.clearNetworks()
        refresh()
        return true
    }

    override suspend fun addManual(network: WifiNetwork): Boolean {
        val rows = manualRows(network, System.currentTimeMillis())
        return wifiNetworkDao.insertIfAbsent(rows).any { it != -1L }
    }

    override suspend fun saveToSystem(networks: List<WifiNetwork>): Boolean {
        if (!privilegedManager.currentMode.hasPrivilegedAccess) return false
        val configs = getPrivilegedConfiguredNetworks()
        networks.forEach { markNotExistInSystem(replacedNetworks(configs, it)) }
        val saved =
            networks
                .flatMap { it.copy(existInSystem = false).toWifiConfigurations() }
                .map { addOrUpdateNetworkPrivileged(it) }
                .all { it }
        refresh()
        return saved
    }

    override suspend fun importNetworks(
        networks: List<WifiNetwork>,
        toDevice: Boolean,
    ): ImportResult {
        val cards =
            networks
                .groupBy { it.ssid to it.password }
                .values
                .map { duplicateNetworks ->
                    val notes =
                        duplicateNetworks
                            .mapNotNull { it.note?.trim() }
                            .filter { it.isNotBlank() }
                            .distinct()
                    duplicateNetworks
                        .first()
                        .copy(
                            securityType = duplicateNetworks.flatMap { it.securityType }.toSet(),
                            note = notes.joinToString("\n").ifEmpty { null },
                        )
                }
        if (cards.isEmpty()) return ImportResult.NOTHING_TO_IMPORT

        val now = System.currentTimeMillis()
        val addedAny = wifiNetworkDao.importNetworks(cards.flatMap { manualRows(it, now) })
        val savedToDevice =
            toDevice && saveToSystem(cards.groupBy { it.ssid }.values.map { it.first() })
        return when {
            toDevice && savedToDevice -> ImportResult.SAVED_TO_DEVICE
            toDevice -> ImportResult.DEVICE_REJECTED_SOME
            addedAny -> ImportResult.SAVED_TO_APP
            else -> ImportResult.ALREADY_SAVED
        }
    }

    override suspend fun persistEphemeralNetworks() =
        withDataSource(Unit) { it.persistEphemeralNetworks() }

    override suspend fun updateNote(ssid: String, password: String, note: String?) {
        wifiNetworkDao.updateNote(ssid, password, note)
    }

    override suspend fun disconnect(): Boolean = withDataSource(false) { it.disconnect() }

    override suspend fun connect(network: WifiNetwork): Boolean =
        withContext(NonCancellable) {
            val configs = getPrivilegedConfiguredNetworks()
            val (same, others) =
                replacedNetworks(configs, network).partition { it.password == network.password }

            withDataSource(Unit) { source ->
                if (same.isNotEmpty()) {
                    same.any { source.connect(netId = it.networkId) }
                    return@withDataSource
                }

                others.map { it.networkId }.toSet().map { async { forget(it) } }.awaitAll()
                markNotExistInSystem(others)
                for (config in network.copy(existInSystem = false).toWifiConfigurations()) {
                    if (source.connect(config = config)) return@withDataSource
                }
            }

            val connected =
                withTimeoutOrNull(10.seconds) {
                    getConnectedNetworkIdFlow().first { id ->
                        getPrivilegedConfiguredNetworks()
                            .map(WifiNetwork::fromWifiConfiguration)
                            .any {
                                it.networkId == id &&
                                    it.ssid == network.ssid &&
                                    it.password == network.password
                            }
                    }
                } != null

            if (!connected && same.isEmpty()) {
                getPrivilegedConfiguredNetworks()
                    .map(WifiNetwork::fromWifiConfiguration)
                    .filter { it.ssid == network.ssid && it.password == network.password }
                    .forEach { forget(it.networkId) }
                others
                    .flatMap { it.copy(existInSystem = false).toWifiConfigurations() }
                    .forEach { addOrUpdateNetworkPrivileged(it) }
            }

            refresh()
            connected
        }
}
