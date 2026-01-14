@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.data.datasource.wifi

import android.content.AttributionSource
import android.content.AttributionSourceHidden
import android.content.Context
import android.net.wifi.IWifiManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfigurationHidden
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import dev.rikka.tools.refine.Refine
import io.github.wifi_password_manager.utils.WifiManagerHelper
import io.github.wifi_password_manager.utils.hasShizukuPermission
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

class ShizukuWifiDataSourceImpl(private val context: Context) : WifiDataSource {
    companion object {
        private const val TAG = "ShizukuManager"
        private const val SHELL_PACKAGE = "com.android.shell"
    }

    private val wifiManager: IWifiManager by lazy {
        SystemServiceHelper.getSystemService(Context.WIFI_SERVICE)
            .let(::ShizukuBinderWrapper)
            .let(IWifiManager.Stub::asInterface)
    }

    private val user: String
        get() =
            when (val uid = Shizuku.getUid()) {
                0 -> "root"
                1000 -> "system"
                2000 -> "shell"
                else -> throw IllegalArgumentException("Unknown Shizuku user $uid")
            }

    private val attributionSource: AttributionSource? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Refine.unsafeCast(
                AttributionSourceHidden(Shizuku.getUid(), SHELL_PACKAGE, SHELL_PACKAGE, null, null)
            )
        } else {
            null
        }
    }

    override suspend fun getPrivilegedConfiguredNetworks(): List<WifiConfiguration> {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, returning empty list")
            return emptyList()
        }

        val extras =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Bundle().apply {
                    putParcelable("EXTRA_PARAM_KEY_ATTRIBUTION_SOURCE", attributionSource)
                }
            } else {
                null
            }

        return WifiManagerHelper.getWifiConfigurations(
            wifiManager = wifiManager,
            packageName = user,
            featureId = SHELL_PACKAGE,
            extras = extras,
        )
    }

    override suspend fun addOrUpdateNetworkPrivileged(config: WifiConfiguration): Boolean {
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

    override suspend fun removeNetwork(netId: Int): Boolean {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, cannot remove network")
            return false
        }

        return wifiManager.removeNetwork(netId, user)
    }

    override suspend fun persistEphemeralNetworks() {
        val configs = getPrivilegedConfiguredNetworks()
        val ephemeralConfigs =
            configs.filter { Refine.unsafeCast<WifiConfigurationHidden>(it).isEphemeral }

        for (config in ephemeralConfigs) {
            val hiddenConfig =
                Refine.unsafeCast<WifiConfigurationHidden>(config).apply {
                    ephemeral = false
                    fromWifiNetworkSuggestion = false
                }
            val wifiConfig = Refine.unsafeCast<WifiConfiguration>(hiddenConfig)
            addOrUpdateNetworkPrivileged(wifiConfig)
        }
    }
}
