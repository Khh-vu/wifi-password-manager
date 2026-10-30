package io.github.wifi_password_manager.services

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Process
import android.system.Os
import kotlin.system.exitProcess
import rikka.shizuku.ShizukuApiConstants

class WiFiShizukuService : WiFiRootServiceImpl() {
    init {
        @Suppress("DEPRECATION") Os.setuid(Process.SYSTEM_UID)
    }

    @SuppressLint("RestrictedApi")
    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        if (code == ShizukuApiConstants.USER_SERVICE_TRANSACTION_destroy) exitProcess(0)
        return super.onTransact(code, data, reply, flags)
    }
}
