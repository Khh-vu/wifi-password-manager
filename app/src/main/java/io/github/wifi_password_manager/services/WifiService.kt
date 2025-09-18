@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.services

import android.content.AttributionSource
import android.content.AttributionSourceHidden
import android.content.Context
import android.net.wifi.IWifiManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import dev.rikka.tools.refine.Refine
import io.github.wifi_password_manager.data.WifiNetwork
import io.github.wifi_password_manager.utils.fromWifiConfiguration
import io.github.wifi_password_manager.utils.groupAndSortedBySsid
import io.github.wifi_password_manager.utils.hasShizukuPermission
import io.github.wifi_password_manager.utils.toWifiConfigurations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.invoke
import kotlinx.serialization.json.Json
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

class WifiService(private val context: Context, private val json: Json) {
    companion object {
        private const val TAG = "WifiService"

        private const val SHELL_PACKAGE = "com.android.shell"
    }

    private val _configuredNetworks = MutableSharedFlow<List<WifiNetwork>>()
    val configuredNetworks = _configuredNetworks.asSharedFlow()

    private val wifiManager by lazy {
        SystemServiceHelper.getSystemService(Context.WIFI_SERVICE)
            .let(::ShizukuBinderWrapper)
            .let(IWifiManager.Stub::asInterface)
    }

    private val user
        get() =
            when (Shizuku.getUid()) {
                0 -> "root"
                1000 -> "system"
                2000 -> "shell"
                else -> throw IllegalArgumentException("Unknown Shizuku user ${Shizuku.getUid()}")
            }

    private val attributionSource: AttributionSource? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Refine.unsafeCast(
                AttributionSourceHidden(
                    Shizuku.getUid(),
                    SHELL_PACKAGE,
                    SHELL_PACKAGE,
                    null as Set<String>?,
                    null,
                )
            )
        } else {
            null
        }
    }

    suspend fun refresh() {
        val networks = getPrivilegedConfiguredNetworks()
        _configuredNetworks.emit(value = networks)
    }

    fun getPrivilegedConfiguredNetworks(): List<WifiNetwork> {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, returning empty list")
            return emptyList()
        }
        return runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val bundle =
                        Bundle().apply {
                            putParcelable("EXTRA_PARAM_KEY_ATTRIBUTION_SOURCE", attributionSource)
                        }
                    wifiManager.getPrivilegedConfiguredNetworks(user, SHELL_PACKAGE, bundle)?.list
                } else {
                    wifiManager.getPrivilegedConfiguredNetworks(user, SHELL_PACKAGE)?.list
                }
            }
            .fold(
                onSuccess = { configurations ->
                    configurations
                        ?.map(WifiNetwork::fromWifiConfiguration)
                        .orEmpty()
                        .groupAndSortedBySsid()
                        .also { Log.d(TAG, "Found ${it.size} networks") }
                },
                onFailure = {
                    Log.e(TAG, "Error getting configured networks", it)
                    emptyList()
                },
            )
    }

    suspend fun addOrUpdateNetworks(networks: List<WifiNetwork>) {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, cannot add/update networks")
            return
        }

        if (networks.isEmpty()) return

        runCatching {
                Dispatchers.IO {
                    networks
                        .flatMap { network ->
                            network.toWifiConfigurations().map { config ->
                                async {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val result =
                                            wifiManager.addOrUpdateNetworkPrivileged(config, user)

                                        result?.statusCode ==
                                            WifiManager.AddNetworkResult.STATUS_SUCCESS
                                    } else {
                                        wifiManager.addOrUpdateNetwork(config, user) != -1
                                    }
                                }
                            }
                        }
                        .awaitAll()
                }
            }
            .fold(
                onSuccess = { results ->
                    refresh()
                    Log.d(TAG, "Added or Updated ${results.count { it }}/${results.size} networks")
                },
                onFailure = { Log.e(TAG, "Error adding or updating networks", it) },
            )
    }

    suspend fun removeAllNetworks() {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, cannot remove networks")
            return
        }

        val networks = getPrivilegedConfiguredNetworks()
        val validNetworks = networks.filter { it.networkId != -1 }

        if (validNetworks.isEmpty()) {
            Log.d(TAG, "No networks to remove")
            return
        }

        runCatching {
                Dispatchers.IO {
                    validNetworks
                        .map { network ->
                            async { wifiManager.removeNetwork(network.networkId, user) }
                        }
                        .awaitAll()
                }
            }
            .fold(
                onSuccess = { results ->
                    refresh()
                    Log.d(TAG, "Removed ${results.count { it }}/${results.size} networks")
                },
                onFailure = { Log.e(TAG, "Error removing all networks", it) },
            )
    }

    suspend fun exportToJson(): String =
        Dispatchers.Default {
            val networks = getPrivilegedConfiguredNetworks()
            json.encodeToString(networks)
        }

    suspend fun getNetworks(jsonString: String): List<WifiNetwork> =
        Dispatchers.Default { json.decodeFromString(jsonString) }
}
