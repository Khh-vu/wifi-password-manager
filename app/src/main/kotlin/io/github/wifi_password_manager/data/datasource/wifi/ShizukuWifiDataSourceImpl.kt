package io.github.wifi_password_manager.data.datasource.wifi

import android.content.AttributionSource
import android.content.AttributionSourceHidden
import android.content.Context
import android.net.wifi.IActionListener
import android.net.wifi.IWifiManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfigurationHidden
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.util.Log
import dev.rikka.tools.refine.Refine
import io.github.wifi_password_manager.utils.WifiManagerHelper
import io.github.wifi_password_manager.utils.awaitAction
import io.github.wifi_password_manager.utils.hasShizukuPermission
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

class ShizukuWifiDataSourceImpl(private val context: Context) : WifiDataSource {
    companion object {
        private const val TAG = "ShizukuWifiDataSource"
        private const val SHELL_PACKAGE = "com.android.shell"
    }

    private val wifiManager: IWifiManager by lazy {
        SystemServiceHelper.getSystemService(Context.WIFI_SERVICE).let(::ShizukuBinderWrapper)
            .let(IWifiManager.Stub::asInterface)
    }

    private val user: String
        get() = when (val uid = Shizuku.getUid()) {
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

        val extras = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

    override suspend fun forget(netId: Int): Boolean {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, cannot forget network")
            return false
        }

        return awaitAction { listener ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                wifiManager.forget(netId, listener)
            } else {
                wifiManager.forget(netId, Binder(), listener, listener.hashCode())
            }
        }
    }

    override suspend fun getConnectionInfo(): WifiInfo? {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, returning null")
            return null
        }
        if (wifiManager.wifiEnabledState != WifiManager.WIFI_STATE_ENABLED) return null
        return wifiManager.getConnectionInfo(SHELL_PACKAGE, null)
    }

    override suspend fun persistEphemeralNetworks() {
        val configs = getPrivilegedConfiguredNetworks()
        val ephemeralConfigs = configs.filter {
            Refine.unsafeCast<WifiConfigurationHidden>(it).isEphemeral
        }

        for (config in ephemeralConfigs) {
            val hiddenConfig = Refine.unsafeCast<WifiConfigurationHidden>(config).apply {
                ephemeral = false
                fromWifiNetworkSuggestion = false
            }
            val wifiConfig = Refine.unsafeCast<WifiConfiguration>(hiddenConfig)
            addOrUpdateNetworkPrivileged(wifiConfig)
        }
    }

    override suspend fun disconnect(): Boolean {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, cannot disconnect")
            return false
        }
        return wifiManager.disconnect(SHELL_PACKAGE)
    }

    override suspend fun connect(config: WifiConfiguration, listener: IActionListener) {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, cannot connect to network")
            listener.onFailure(0)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            wifiManager.connect(
                config,
                -1,
                listener,
                SHELL_PACKAGE,
                Bundle(),
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            wifiManager.connect(config, -1, listener, SHELL_PACKAGE)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            wifiManager.connect(config, -1, listener)
        } else {
            wifiManager.connect(
                config,
                -1,
                Binder(),
                listener,
                listener.hashCode(),
            )
        }
    }
}
