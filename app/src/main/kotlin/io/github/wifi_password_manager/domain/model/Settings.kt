package io.github.wifi_password_manager.domain.model

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.stringResource
import io.github.wifi_password_manager.R
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Settings(
    val language: Language = Language.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useMaterialYou: Boolean = true,
    val autoPersistEphemeralNetworks: Boolean = false,
    val appLockEnabled: Boolean = false,
    val secureScreenEnabled: Boolean = false,
    val allowCacheMode: Boolean = false,
    val allowInsecureReceiver: Boolean = false,
    val plaintextPasswords: Boolean = false,
    val pureBlackTheme: Boolean = false,
) {
    @Serializable
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM;

        val isDark: Boolean
            @Composable @ReadOnlyComposable get() = when (this) {
                LIGHT -> false
                DARK -> true
                SYSTEM -> isSystemInDarkTheme()
            }

        val resId: Int
            @StringRes get() = when (this) {
                LIGHT -> R.string.theme_mode_light
                DARK -> R.string.theme_mode_dark
                SYSTEM -> R.string.theme_mode_system
            }
    }

    @Serializable
    enum class Language {
        SYSTEM, ENGLISH, RUSSIAN, CHINESE_SIMPLIFIED, JAPANESE, TURKISH, ARABIC;

        val code: String
            get() = when (this) {
                SYSTEM -> ""
                ENGLISH -> "en"
                RUSSIAN -> "ru"
                CHINESE_SIMPLIFIED -> "zh-CN"
                JAPANESE -> "ja"
                TURKISH -> "tr"
                ARABIC -> "ar"
            }

        val displayName: String
            @Composable get() = when (this) {
                SYSTEM -> stringResource(R.string.language_system_default)
                ENGLISH -> "English"
                RUSSIAN -> "Русский"
                CHINESE_SIMPLIFIED -> "中文 (简体)"
                JAPANESE -> "日本語"
                TURKISH -> "Türkçe"
                ARABIC -> "العربية"
            }
    }
}

val LocalSettings = compositionLocalOf { Settings() }
