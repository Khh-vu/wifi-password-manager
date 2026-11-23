package io.github.wifi_password_manager.domain.repository

import android.net.wifi.WifiConfiguration
import io.github.wifi_password_manager.domain.model.ImportResult
import io.github.wifi_password_manager.domain.model.WifiNetwork
import kotlinx.coroutines.flow.Flow

interface WifiRepository {
    fun getConnectedNetworkIdFlow(): Flow<Int>

    fun getAllNetworks(): Flow<List<WifiNetwork>>

    fun getAllNetworks(query: String): Flow<List<WifiNetwork>>

    suspend fun getAllNetworksList(): List<WifiNetwork>

    suspend fun getNetworkCount(): Int

    suspend fun refresh(): Boolean

    suspend fun getPrivilegedConfiguredNetworks(): List<WifiConfiguration>

    suspend fun addOrUpdateNetworkPrivileged(config: WifiConfiguration): Boolean

    suspend fun forget(netId: Int): Boolean

    suspend fun forgetNetwork(network: WifiNetwork)

    suspend fun forgetAll(clearCache: Boolean): Boolean

    suspend fun addManual(network: WifiNetwork): Boolean

    suspend fun saveToSystem(networks: List<WifiNetwork>): Boolean

    suspend fun importNetworks(networks: List<WifiNetwork>, toDevice: Boolean): ImportResult

    suspend fun persistEphemeralNetworks()

    suspend fun updateNote(ssid: String, password: String, note: String?)

    suspend fun disconnect(): Boolean

    suspend fun connect(network: WifiNetwork): Boolean
}
