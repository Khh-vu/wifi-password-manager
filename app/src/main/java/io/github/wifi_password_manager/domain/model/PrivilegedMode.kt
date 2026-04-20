package io.github.wifi_password_manager.domain.model

enum class PrivilegedMode {
    NONE,
    SHIZUKU,
    ROOT;

    val hasPrivilegedAccess: Boolean
        get() = this == SHIZUKU || this == ROOT
}
