package io.github.wifi_password_manager.manager

import android.content.Context
import com.topjohnwu.superuser.Shell
import io.github.wifi_password_manager.domain.model.PrivilegedMode
import io.github.wifi_password_manager.utils.hasShizukuPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PrivilegedManager(private val context: Context) {
    private val _mode = MutableStateFlow(detectPrivilegedMode())
    val mode = _mode.asStateFlow()

    val currentMode: PrivilegedMode
        get() = mode.value

    fun refresh() = _mode.update { detectPrivilegedMode() }

    private fun detectPrivilegedMode(): PrivilegedMode =
        when {
            Shell.isAppGrantedRoot() == true -> PrivilegedMode.ROOT
            context.hasShizukuPermission -> PrivilegedMode.SHIZUKU
            else -> PrivilegedMode.NONE
        }
}
