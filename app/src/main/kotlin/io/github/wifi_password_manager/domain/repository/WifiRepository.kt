package io.github.wifi_password_manager.domain.repository

import android.net.wifi.WifiConfiguration
import io.github.wifi_password_manager.domain.model.WifiNetwork
import kotlinx.coroutines.flow.Flow

interface WifiRepository {
    fun getConnectedWifiSsidFlow(): Flow<String>

    fun getAllNetworks(): Flow<List<WifiNetwork>>

    fun getAllNetworks(query: String): Flow<List<WifiNetwork>>

    suspend fun getAllNetworksList(): List<WifiNetwork>

    suspend fun getNetworkCount(): Int

    suspend fun refresh()

    suspend fun getPrivilegedConfiguredNetworks(): List<WifiConfiguration>

    suspend fun addOrUpdateNetworkPrivileged(config: WifiConfiguration): Boolean

    suspend fun forget(netId: Int): Boolean

    suspend fun delete(ssid: String)

    suspend fun persistEphemeralNetworks()

    suspend fun updateNote(ssid: String, note: String?)

    suspend fun disconnect(): Boolean

    suspend fun connect(network: WifiNetwork)
}
