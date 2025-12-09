@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.services

import android.content.AttributionSource
import android.content.AttributionSourceHidden
import android.content.Context
import android.net.wifi.IWifiManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import dev.rikka.tools.refine.Refine
import io.github.wifi_password_manager.utils.hasShizukuPermission
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

class WifiService(private val context: Context) {
    companion object {
        private const val TAG = "WifiService"

        private const val SHELL_PACKAGE = "com.android.shell"
    }

    private val _configuredNetworks = MutableSharedFlow<List<WifiConfiguration>>()
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
                AttributionSourceHidden(Shizuku.getUid(), SHELL_PACKAGE, SHELL_PACKAGE, null, null)
            )
        } else {
            null
        }
    }

    suspend fun refresh() {
        val networks = getPrivilegedConfiguredNetworks()
        _configuredNetworks.emit(value = networks)
    }

    fun getPrivilegedConfiguredNetworks(): List<WifiConfiguration> {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, returning empty list")
            return emptyList()
        }
        val configs =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val bundle =
                    Bundle().apply {
                        putParcelable("EXTRA_PARAM_KEY_ATTRIBUTION_SOURCE", attributionSource)
                    }
                wifiManager.getPrivilegedConfiguredNetworks(user, SHELL_PACKAGE, bundle)?.list
            } else {
                wifiManager.getPrivilegedConfiguredNetworks(user, SHELL_PACKAGE)?.list
            }
        return configs.orEmpty()
    }

    fun addOrUpdateNetworkPrivileged(config: WifiConfiguration): Boolean {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, cannot add/update network")
            return false
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val result = wifiManager.addOrUpdateNetworkPrivileged(config, user)
            result?.statusCode == WifiManager.AddNetworkResult.STATUS_SUCCESS
        } else {
            wifiManager.addOrUpdateNetwork(config, user) != -1
        }
    }

    fun removeNetwork(netId: Int): Boolean {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, cannot remove network")
            return false
        }
        return wifiManager.removeNetwork(netId, user)
    }
}
