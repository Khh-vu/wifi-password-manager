package io.github.wifi_password_manager.data

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useMaterialYou: Boolean = true,
) {
    @Serializable
    enum class ThemeMode {
        LIGHT,
        DARK,
        SYSTEM;

        val isDark: Boolean
            @Composable
            get() =
                when (this) {
                    LIGHT -> false
                    DARK -> true
                    SYSTEM -> isSystemInDarkTheme()
                }
    }
}
