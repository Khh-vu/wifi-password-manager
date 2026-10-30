package io.github.wifi_password_manager.services

import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.system.Os
import com.topjohnwu.superuser.ipc.RootService

class WiFiRootService : RootService() {
    override fun onBind(intent: Intent): IBinder {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION") Os.setuid(Process.SYSTEM_UID)
        }
        return WiFiRootServiceImpl()
    }
}
