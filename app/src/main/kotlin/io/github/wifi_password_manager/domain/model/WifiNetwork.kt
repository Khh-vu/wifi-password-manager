package io.github.wifi_password_manager.domain.model

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
    enum class SecurityType {
        OPEN,
        OWE,
        WPA2,
        WPA3,
        WEP,
    }

    @Serializable
    enum class MeteredOverride {
        NONE,
        METERED,
        NOT_METERED,
    }

    @Serializable
    enum class Randomization {
        NONE,
        PERSISTENT,
        NON_PERSISTENT,
        AUTO,
    }

    val securityLabel: String
        @Composable
        @ReadOnlyComposable
        get() {
            val resources = LocalResources.current
            return securityType
                .sortedBy { it.ordinal }
                .joinToString("/") {
                    resources.getString(
                        when (it) {
                            SecurityType.OPEN -> R.string.security_open
                            SecurityType.OWE -> R.string.security_owe
                            SecurityType.WPA2 -> R.string.security_wpa2
                            SecurityType.WPA3 -> R.string.security_wpa3
                            SecurityType.WEP -> R.string.security_wep
                        },
                    )
                }
        }
}
