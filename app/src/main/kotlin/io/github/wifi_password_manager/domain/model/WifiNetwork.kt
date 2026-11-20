package io.github.wifi_password_manager.domain.model

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalResources
import io.github.wifi_password_manager.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Immutable
@Serializable
data class WifiNetwork(
    @Transient val networkId: Int = -1,
    val ssid: String,
    @SerialName("security") val securityType: Set<SecurityType>,
    val password: String,
    val hidden: Boolean = false,
    val autojoin: Boolean = true,
    val private: Boolean = false,
    val note: String? = null,
    @Transient val existInSystem: Boolean = true,
    val meteredOverride: MeteredOverride = MeteredOverride.NONE,
    val macRandomizationSetting: Randomization = Randomization.AUTO,
    @Transient val randomizedMacAddress: String? = null,
    @Transient val createdAt: Long? = null,
    @Transient val updatedAt: Long? = null,
) {
    @Serializable
    enum class SecurityType(@StringRes val labelResId: Int) {
        OPEN(R.string.security_open),
        OWE(R.string.security_owe),
        WPA2(R.string.security_wpa2),
        WPA3(R.string.security_wpa3),
        WEP(R.string.security_wep);

        companion object {
            val withoutPassword: Set<SecurityType> = setOf(OPEN, OWE)
            val withPassword: Set<SecurityType> = setOf(WPA2, WPA3, WEP)
        }
    }

    @Serializable
    enum class MeteredOverride(@StringRes val labelResId: Int) {
        NONE(R.string.metered_auto),
        METERED(R.string.metered_metered),
        NOT_METERED(R.string.metered_unmetered),
    }

    @Serializable
    enum class Randomization(@StringRes val labelResId: Int) {
        NONE(R.string.mac_randomization_none),
        PERSISTENT(R.string.mac_randomization_persistent),
        NON_PERSISTENT(R.string.mac_randomization_non_persistent),
        AUTO(R.string.mac_randomization_auto);

        companion object {
            val supported: List<Randomization>
                get() =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        entries
                    } else {
                        listOf(NONE, PERSISTENT)
                    }

            val default: Randomization
                get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) AUTO else PERSISTENT
        }
    }

    val securityLabel: String
        @Composable
        @ReadOnlyComposable
        get() {
            val resources = LocalResources.current
            return securityType
                .sortedBy { it.ordinal }
                .joinToString("/") { resources.getString(it.labelResId) }
        }
}
