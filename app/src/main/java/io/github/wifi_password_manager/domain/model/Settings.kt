package io.github.wifi_password_manager.domain.model

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import io.github.wifi_password_manager.R
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Settings(
    val language: Language = Language.ENGLISH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useMaterialYou: Boolean = true,
    val autoPersistEphemeralNetworks: Boolean = false,
    val appLockEnabled: Boolean = false,
    val secureScreenEnabled: Boolean = false,
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

        val resId: Int
            @StringRes
            get() =
                when (this) {
                    LIGHT -> R.string.theme_mode_light
                    DARK -> R.string.theme_mode_dark
                    SYSTEM -> R.string.theme_mode_system
                }
    }

    @Serializable
    enum class Language(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        RUSSIAN("ru", "Русский"),
    }
}
