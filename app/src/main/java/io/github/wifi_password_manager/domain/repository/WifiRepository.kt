@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.domain.repository

import android.net.wifi.WifiConfiguration
import kotlinx.coroutines.flow.Flow

interface WifiRepository {
    val configuredNetworks: Flow<List<WifiConfiguration>>

    suspend fun refresh()

    fun getPrivilegedConfiguredNetworks(): List<WifiConfiguration>

    fun addOrUpdateNetworkPrivileged(config: WifiConfiguration): Boolean

    fun removeNetwork(netId: Int): Boolean
}
