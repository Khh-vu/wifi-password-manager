package io.github.wifi_password_manager.services

import android.content.Intent
import android.os.IBinder
import com.topjohnwu.superuser.ipc.RootService

class WiFiRootService : RootService() {
    override fun onBind(intent: Intent): IBinder {
        return WiFiRootServiceImpl()
    }
}
