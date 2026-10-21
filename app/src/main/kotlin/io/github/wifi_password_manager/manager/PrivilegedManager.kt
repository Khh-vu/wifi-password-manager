package io.github.wifi_password_manager.manager

import android.content.Context
import com.topjohnwu.superuser.Shell
import io.github.wifi_password_manager.domain.model.PrivilegedMode
import io.github.wifi_password_manager.utils.hasShizukuPermission
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class PrivilegedManager(private val context: Context) {
    val mode: StateFlow<PrivilegedMode>
        field = MutableStateFlow(PrivilegedMode.NONE)

    val currentMode: PrivilegedMode
        get() = mode.value

    suspend fun refresh() = mode.update { detectPrivilegedMode() }

    private suspend fun detectPrivilegedMode(): PrivilegedMode =
        when {
            withContext(Shell.EXECUTOR.asCoroutineDispatcher()) { Shell.getShell().isRoot } ->
                PrivilegedMode.ROOT
            context.hasShizukuPermission -> PrivilegedMode.SHIZUKU
            else -> PrivilegedMode.NONE
        }
}
