package io.github.wifi_password_manager.utils

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import io.github.wifi_password_manager.domain.model.Settings
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

val Context.hasShizukuPermission: Boolean
    get() {
        if (!Shizuku.pingBinder()) return false
        return if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
            ContextCompat.checkSelfPermission(this, ShizukuProvider.PERMISSION)
        } else {
            Shizuku.checkSelfPermission()
        } == PackageManager.PERMISSION_GRANTED
    }

fun Context.launchUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (_: Exception) {}
}

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, text, duration).show()

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, resId, duration).show()

fun Context.isBiometricAuthenticationSupported(): Boolean {
    val authenticators = Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL
    return BiometricManager.from(this).canAuthenticate(authenticators) ==
        BiometricManager.BIOMETRIC_SUCCESS
}

fun Context.setAppTheme(themeMode: Settings.ThemeMode) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService<UiModeManager>()
            ?.setApplicationNightMode(
                when (themeMode) {
                    Settings.ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
                    Settings.ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
                    Settings.ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
                }
            )
    } else {
        AppCompatDelegate.setDefaultNightMode(
            when (themeMode) {
                Settings.ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                Settings.ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                Settings.ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }
}
