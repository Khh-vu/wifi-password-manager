package io.github.wifi_password_manager.manager

import android.content.Context
import android.os.Build
import android.os.Process
import com.topjohnwu.superuser.Shell
import io.github.wifi_password_manager.domain.model.PrivilegedMode
import io.github.wifi_password_manager.utils.hasShizukuPermission
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

class PrivilegedManager(private val context: Context) {
    val isShizukuUnsupported: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val mode: StateFlow<PrivilegedMode>
        field = MutableStateFlow(PrivilegedMode.NONE)

    val currentMode: PrivilegedMode
        get() = mode.value

    suspend fun refresh() {
        isShizukuUnsupported.update { detectShizukuUnsupported() }
        mode.update { detectPrivilegedMode() }
    }

    private fun detectShizukuUnsupported(): Boolean {
        if (!Shizuku.pingBinder()) return false
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.Q &&
            Shizuku.getUid() !in setOf(Process.ROOT_UID, Process.SYSTEM_UID)
    }

    private suspend fun detectPrivilegedMode(): PrivilegedMode =
        when {
            withContext(Shell.EXECUTOR.asCoroutineDispatcher()) { Shell.getShell().isRoot } ->
                PrivilegedMode.ROOT

            context.hasShizukuPermission && !isShizukuUnsupported.value -> PrivilegedMode.SHIZUKU

            else -> PrivilegedMode.NONE
        }
}
