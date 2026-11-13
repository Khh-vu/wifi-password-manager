package io.github.wifi_password_manager.manager

import android.net.wifi.IWifiManager
import android.os.Build
import android.util.Log
import io.github.wifi_password_manager.BuildConfig
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.lsposed.hiddenapibypass.HiddenApiBypass

val Throwable.isSystemApiMismatch: Boolean
    get() =
        this is LinkageError ||
            this is NoSuchMethodException ||
            this is UnsupportedOperationException

class SystemApiErrorReporter(private val privilegedManager: PrivilegedManager) {
    companion object {
        private const val TAG = "SystemApiErrorReporter"

        private val USED_METHODS =
            setOf(
                "getPrivilegedConfiguredNetworks",
                "addOrUpdateNetworkPrivileged",
                "addOrUpdateNetwork",
                "removeNetwork",
                "enableNetwork",
                "reconnect",
                "getConnectionInfo",
                "getWifiEnabledState",
                "disconnect",
                "connect",
                "forget",
            )
    }

    private val reported = ConcurrentHashMap.newKeySet<String>()
    val report: StateFlow<String?>
        field = MutableStateFlow<String?>(null)

    fun report(error: Throwable) {
        Log.e(TAG, "System API mismatch", error)
        if (!reported.add(error.toString())) return
        report.update { _ ->
            buildString {
                appendLine("App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                appendLine("Build: ${Build.FINGERPRINT}")
                appendLine("Mode: ${privilegedManager.currentMode}")

                appendLine()

                appendLine("IWifiManager methods on this device:")
                HiddenApiBypass.getDeclaredMethods(IWifiManager::class.java)
                    .filter { it.name in USED_METHODS }
                    .map { it.toGenericString() }
                    .forEach(::appendLine)

                appendLine()

                appendLine("Error:")
                append(error.stackTraceToString())
            }
        }
    }

    fun dismiss() {
        report.update { null }
        reported.clear()
    }
}
