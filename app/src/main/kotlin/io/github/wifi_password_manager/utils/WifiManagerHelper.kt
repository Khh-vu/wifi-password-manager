package io.github.wifi_password_manager.utils

import android.net.wifi.IWifiManager
import android.net.wifi.WifiConfiguration
import android.os.Build
import android.os.Bundle
import org.lsposed.hiddenapibypass.HiddenApiBypass

object WifiManagerHelper {
    @Suppress("UNCHECKED_CAST")
    fun getWifiConfigurations(
        wifiManager: IWifiManager,
        packageName: String,
        featureId: String,
        extras: Bundle? = null,
    ): List<WifiConfiguration> {
        val networks =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val method =
                    HiddenApiBypass.getDeclaredMethod(
                        IWifiManager::class.java,
                        "getPrivilegedConfiguredNetworks",
                        String::class.java,
                        String::class.java,
                        Bundle::class.java,
                    )
                method(wifiManager, packageName, featureId, extras)
            } else {
                try {
                    val method =
                        HiddenApiBypass.getDeclaredMethod(
                            IWifiManager::class.java,
                            "getPrivilegedConfiguredNetworks",
                            String::class.java,
                            String::class.java,
                        )
                    method(wifiManager, packageName, featureId)
                } catch (_: NoSuchMethodException) {
                    val method =
                        HiddenApiBypass.getDeclaredMethod(
                            IWifiManager::class.java,
                            "getPrivilegedConfiguredNetworks",
                            String::class.java,
                            String::class.java,
                            Bundle::class.java,
                        )
                    method(wifiManager, packageName, featureId, extras)
                }
            }
        val result =
            try {
                networks::class.java.getMethod("getList")(networks) as? List<WifiConfiguration>?
            } catch (_: Exception) {
                null
            }
        return result.orEmpty()
    }
}
