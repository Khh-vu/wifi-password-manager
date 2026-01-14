@file:Suppress("DEPRECATION", "UNCHECKED_CAST")

package io.github.wifi_password_manager.utils

import android.net.wifi.IWifiManager
import android.net.wifi.WifiConfiguration
import android.os.Build
import android.os.Bundle
import java.lang.reflect.Method
import org.lsposed.hiddenapibypass.HiddenApiBypass

object WifiManagerHelper {
    // Use reflection because of method signature conflict in IWifiManager
    private val getPrivilegedConfiguredNetworks: Method by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            HiddenApiBypass.getDeclaredMethod(
                IWifiManager::class.java,
                "getPrivilegedConfiguredNetworks",
                String::class.java,
                String::class.java,
                Bundle::class.java,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            HiddenApiBypass.getDeclaredMethod(
                IWifiManager::class.java,
                "getPrivilegedConfiguredNetworks",
                String::class.java,
                String::class.java,
            )
        } else {
            HiddenApiBypass.getDeclaredMethod(
                IWifiManager::class.java,
                "getPrivilegedConfiguredNetworks",
                String::class.java,
                String::class.java,
            )
        }
    }

    fun getWifiConfigurations(
        wifiManager: IWifiManager,
        packageName: String,
        featureId: String,
        extras: Bundle? = null,
    ): List<WifiConfiguration> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                val result =
                    getPrivilegedConfiguredNetworks(wifiManager, packageName, featureId, extras)
                        as?
                        com.android.wifi.x.com.android.modules.utils.ParceledListSlice<
                            WifiConfiguration
                        >
                result?.list
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val result =
                    getPrivilegedConfiguredNetworks(wifiManager, packageName, featureId)
                        as?
                        com.android.wifi.x.com.android.modules.utils.ParceledListSlice<
                            WifiConfiguration
                        >
                result?.list
            }
            else -> {
                val result =
                    getPrivilegedConfiguredNetworks(wifiManager, packageName, featureId)
                        as?
                        com.android.wifi.x.android.content.pm.ParceledListSlice<WifiConfiguration>
                result?.list
            }
        }.orEmpty()
    }
}
