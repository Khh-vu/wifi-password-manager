@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.services

import android.content.AttributionSource
import android.content.AttributionSourceHidden
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.IWifiManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfigurationHidden
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import com.topjohnwu.superuser.ipc.RootService
import dev.rikka.tools.refine.Refine
import io.github.wifi_password_manager.IWifiRootService
import io.github.wifi_password_manager.ipc.WifiNetworkParcel
import io.github.wifi_password_manager.utils.WifiManagerHelper
import rikka.shizuku.SystemServiceHelper

class WiFiRootService : RootService() {
    companion object {
        private const val SHELL_PACKAGE = "com.android.shell"
    }

    override fun onBind(intent: Intent): IBinder {
        val intent = Intent(Settings.ACTION_SETTINGS)
        val settingPackage =
            packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_SYSTEM_ONLY)
                .firstOrNull()
                ?.activityInfo
                ?.packageName
        return WiFiRootServiceImpl(settingPackage ?: SHELL_PACKAGE)
    }

    private class WiFiRootServiceImpl(private val packageName: String) : IWifiRootService.Stub() {
        companion object {
            private const val USER = "root"
        }

        private val wifiManager: IWifiManager by lazy {
            SystemServiceHelper.getSystemService(WIFI_SERVICE).let(IWifiManager.Stub::asInterface)
        }

        private val attributionSource: AttributionSource? by lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Refine.unsafeCast(AttributionSourceHidden(0, packageName, packageName, null, null))
            } else {
                null
            }
        }

        private fun getConfiguredNetworks(): List<WifiConfiguration> {
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
                packageName = USER,
                featureId = packageName,
                extras = extras,
            )
        }

        private fun addOrUpdateNetwork(config: WifiConfiguration): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val result = wifiManager.addOrUpdateNetworkPrivileged(config, USER)
                result?.statusCode == WifiManager.AddNetworkResult.STATUS_SUCCESS
            } else {
                wifiManager.addOrUpdateNetwork(config, USER) != -1
            }
        }

        override fun getPrivilegedConfiguredNetworks(): List<WifiNetworkParcel> {
            return getConfiguredNetworks().map(WifiNetworkParcel::fromWifiConfiguration)
        }

        override fun addOrUpdateNetworkPrivileged(config: WifiNetworkParcel): Boolean {
            return addOrUpdateNetwork(config.toWifiConfiguration())
        }

        override fun removeNetwork(netId: Int): Boolean {
            return wifiManager.removeNetwork(netId, USER)
        }

        override fun persistEphemeralNetworks() {
            val ephemeralConfigs =
                getConfiguredNetworks().filter {
                    Refine.unsafeCast<WifiConfigurationHidden>(it).isEphemeral
                }

            for (config in ephemeralConfigs) {
                val hiddenConfig =
                    Refine.unsafeCast<WifiConfigurationHidden>(config).apply {
                        ephemeral = false
                        fromWifiNetworkSuggestion = false
                    }
                addOrUpdateNetwork(Refine.unsafeCast(hiddenConfig))
            }
        }
    }
}
