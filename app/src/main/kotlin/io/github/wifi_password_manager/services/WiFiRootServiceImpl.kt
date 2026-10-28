package io.github.wifi_password_manager.services

import android.content.AttributionSource
import android.content.AttributionSourceHidden
import android.content.Context
import android.net.wifi.IActionListener
import android.net.wifi.IWifiManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfigurationHidden
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Process
import dev.rikka.tools.refine.Refine
import io.github.wifi_password_manager.IWifiRootService
import io.github.wifi_password_manager.ipc.WifiInfoParcel
import io.github.wifi_password_manager.ipc.WifiNetworkParcel
import io.github.wifi_password_manager.utils.WifiManagerHelper
import rikka.shizuku.SystemServiceHelper

class WiFiRootServiceImpl : IWifiRootService.Stub() {
    companion object {
        private const val PACKAGE_NAME = "android"
        private const val USER = "root"
    }

    private val wifiManager: IWifiManager by lazy {
        SystemServiceHelper.getSystemService(Context.WIFI_SERVICE)
            .let(IWifiManager.Stub::asInterface)
    }

    private val attributionSource: AttributionSource?
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Refine.unsafeCast(
                    AttributionSourceHidden(
                        Process.ROOT_UID,
                        PACKAGE_NAME,
                        PACKAGE_NAME,
                        null,
                        null,
                    )
                )
            } else {
                null
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
            featureId = PACKAGE_NAME,
            extras = extras,
        )
    }

    private fun addOrUpdateNetwork(config: WifiConfiguration): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val result = wifiManager.addOrUpdateNetworkPrivileged(config, USER)
            result?.networkId ?: -1
        } else {
            wifiManager.addOrUpdateNetwork(config, USER)
        }
    }

    override fun getPrivilegedConfiguredNetworks(): List<WifiNetworkParcel> {
        return getConfiguredNetworks().map(WifiNetworkParcel::fromWifiConfiguration)
    }

    override fun addOrUpdateNetworkPrivileged(config: WifiNetworkParcel): Int {
        return addOrUpdateNetwork(config.toWifiConfiguration())
    }

    override fun removeNetwork(netId: Int): Boolean {
        return wifiManager.removeNetwork(netId, USER)
    }

    override fun forget(netId: Int, listener: IActionListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            wifiManager.forget(netId, listener)
        } else {
            wifiManager.forget(netId, Binder(), listener, listener.hashCode())
        }
    }

    override fun getConnectionInfo(): WifiInfoParcel? {
        if (wifiManager.wifiEnabledState != WifiManager.WIFI_STATE_ENABLED) return null
        val info = wifiManager.getConnectionInfo(USER, null)
        return info?.let(WifiInfoParcel::fromWifiInfo)
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

    override fun disconnect(): Boolean {
        return wifiManager.disconnect(PACKAGE_NAME)
    }

    override fun connect(config: WifiNetworkParcel?, netId: Int, listener: IActionListener) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM -> {
                wifiManager.connect(
                    config?.toWifiConfiguration(),
                    netId,
                    listener,
                    PACKAGE_NAME,
                    Bundle().apply {
                        putParcelable("EXTRA_PARAM_KEY_ATTRIBUTION_SOURCE", attributionSource)
                    },
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                wifiManager.connect(config?.toWifiConfiguration(), netId, listener, PACKAGE_NAME)
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                wifiManager.connect(config?.toWifiConfiguration(), netId, listener)
            }

            else -> {
                wifiManager.connect(
                    config?.toWifiConfiguration(),
                    netId,
                    Binder(),
                    listener,
                    listener.hashCode(),
                )
            }
        }
    }
}
