package io.github.wifi_password_manager.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

val Context.hasShizukuPermission: Boolean
    get() {
        if (!Shizuku.pingBinder()) return false
        return if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
            ContextCompat.checkSelfPermission(this, ShizukuProvider.PERMISSION) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
    }

fun Context.launchUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (_: Exception) {}
}

fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, text, duration).show()

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, resId, duration).show()
