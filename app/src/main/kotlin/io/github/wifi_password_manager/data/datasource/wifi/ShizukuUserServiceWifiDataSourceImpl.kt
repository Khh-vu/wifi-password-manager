package io.github.wifi_password_manager.data.datasource.wifi

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.util.Log
import io.github.wifi_password_manager.BuildConfig
import io.github.wifi_password_manager.services.WiFiShizukuService
import io.github.wifi_password_manager.utils.hasShizukuPermission
import rikka.shizuku.Shizuku

class ShizukuUserServiceWifiDataSourceImpl(private val context: Context) :
    RootWifiDataSourceImpl(context) {
    companion object {
        private const val TAG = "ShizukuUserServiceWifiDataSource"
    }

    private val args by lazy {
        val componentName = ComponentName(context, WiFiShizukuService::class.java)
        Shizuku.UserServiceArgs(componentName).apply {
            debuggable(BuildConfig.DEBUG)
            processNameSuffix("wifi")
            version(BuildConfig.VERSION_CODE)
            daemon(false)
        }
    }

    override fun bind(connection: ServiceConnection, onUnavailable: () -> Unit) {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available")
            onUnavailable()
            return
        }
        Shizuku.bindUserService(args, connection)
    }

    override fun unbind(connection: ServiceConnection) {
        Shizuku.unbindUserService(args, connection, true)
    }
}
