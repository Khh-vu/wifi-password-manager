package io.github.wifi_password_manager.domain.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import io.github.wifi_password_manager.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Settings(
    val language: Language = Language.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useMaterialYou: Boolean = true,
    val autoPersistEphemeralNetworks: Boolean = false,
    val autoSyncNetworks: Boolean = false,
    val appLockEnabled: Boolean = false,
    val lockTimeout: Duration = 30.seconds,
    val secureScreenEnabled: Boolean = false,
    val allowCacheMode: Boolean = false,
    val allowInsecureReceiver: Boolean = false,
    val plaintextPasswords: Boolean = false,
    val pureBlackTheme: Boolean = false,
    val searchBarAtBottom: Boolean = false,
) {
    @Serializable
    enum class ThemeMode(@StringRes val resId: Int) {
        LIGHT(R.string.theme_mode_light),
        DARK(R.string.theme_mode_dark),
        SYSTEM(R.string.theme_mode_system),
    }

    @Serializable
    enum class Language(val code: String) {
        SYSTEM(""),
        ENGLISH("en"),
        RUSSIAN("ru"),
        CHINESE_SIMPLIFIED("zh-CN"),
        JAPANESE("ja"),
        TURKISH("tr"),
        ARABIC("ar"),
    }
}
