package io.github.wifi_password_manager.domain.model

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useMaterialYou: Boolean = true,
    val autoPersistEphemeralNetworks: Boolean = false,
) {
    @Serializable
    enum class ThemeMode {
        LIGHT,
        DARK,
        SYSTEM;

        val isDark: Boolean
            @Composable
            @ReadOnlyComposable
            get() =
                when (this) {
                    LIGHT -> false
                    DARK -> true
                    SYSTEM -> isSystemInDarkTheme()
                }
    }
}
