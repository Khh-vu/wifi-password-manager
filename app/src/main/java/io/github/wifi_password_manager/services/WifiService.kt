@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.services

import android.content.AttributionSource
import android.content.Context
import android.net.wifi.IWifiManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import io.github.wifi_password_manager.data.WifiNetwork
import kotlinx.serialization.json.Json
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

class WifiService(private val json: Json) {
    companion object {
        private const val TAG = "WifiService"

        private const val SHELL_PACKAGE = "com.android.shell"
    }

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

    fun getPrivilegedConfiguredNetworks(): List<WifiNetwork> =
        runCatching {
                val configs =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        wifiManager
                            .getPrivilegedConfiguredNetworks(
                                user,
                                SHELL_PACKAGE,
                                Bundle().apply {
                                    putParcelable(
                                        "EXTRA_PARAM_KEY_ATTRIBUTION_SOURCE",
                                        AttributionSource::class
                                            .java
                                            .getConstructor(
                                                Int::class.java,
                                                String::class.java,
                                                String::class.java,
                                                Set::class.java,
                                                AttributionSource::class.java,
                                            )
                                            .newInstance(
                                                Shizuku.getUid(),
                                                SHELL_PACKAGE,
                                                SHELL_PACKAGE,
                                                null as Set<String>?,
                                                null,
                                            ),
                                    )
                                },
                            )
                            ?.list
                    } else {
                        wifiManager.getPrivilegedConfiguredNetworks(user, SHELL_PACKAGE)?.list
                    }
                configs?.forEach { Log.d(TAG, it.toString()) }
                configs
            }
            .fold(
                onSuccess = { it?.map(WifiNetwork::fromWifiConfiguration).orEmpty() },
                onFailure = {
                    Log.e(TAG, "Error getting configured networks", it)
                    emptyList()
                },
            )

    fun addOrUpdateNetwork(config: WifiConfiguration): Boolean =
        runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    wifiManager.addOrUpdateNetworkPrivileged(config, SHELL_PACKAGE)?.statusCode ==
                        WifiManager.AddNetworkResult.STATUS_SUCCESS
                } else {
                    wifiManager.addOrUpdateNetwork(config, SHELL_PACKAGE) != -1
                }
            }
            .fold(
                onSuccess = {
                    Log.d(TAG, "Network added or updated: $config")
                    it
                },
                onFailure = {
                    Log.e(TAG, "Error adding or updating network", it)
                    false
                },
            )

    fun removeNetwork(netId: Int): Boolean =
        runCatching { wifiManager.removeNetwork(netId, SHELL_PACKAGE) }
            .fold(
                onSuccess = {
                    Log.d(TAG, "Network removed: $netId")
                    it
                },
                onFailure = {
                    Log.e(TAG, "Error removing network", it)
                    false
                },
            )
}
