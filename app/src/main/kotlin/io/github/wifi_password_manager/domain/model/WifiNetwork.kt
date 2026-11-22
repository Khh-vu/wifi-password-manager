package io.github.wifi_password_manager.domain.model

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalResources
import io.github.wifi_password_manager.R
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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
    @Serializable(with = MeteredOverrideSerializer::class)
    val meteredOverride: Int = MeteredOverride.NONE.value,
    @Serializable(with = RandomizationSerializer::class)
    val macRandomizationSetting: Int = Randomization.default.value,
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

    enum class MeteredOverride(val value: Int, @StringRes val labelResId: Int) {
        NONE(0, R.string.metered_auto),
        METERED(1, R.string.metered_metered),
        NOT_METERED(2, R.string.metered_unmetered);

        companion object {
            fun fromValue(value: Int): MeteredOverride? = entries.find { it.value == value }
        }
    }

    enum class Randomization(val value: Int, @StringRes val labelResId: Int) {
        NONE(0, R.string.mac_randomization_none),
        PERSISTENT(1, R.string.mac_randomization_persistent),
        NON_PERSISTENT(2, R.string.mac_randomization_non_persistent),
        AUTO(3, R.string.mac_randomization_auto);

        companion object {
            fun fromValue(value: Int): Randomization? = entries.find { it.value == value }

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

object MeteredOverrideSerializer : KSerializer<Int> {
    override val descriptor =
        PrimitiveSerialDescriptor("WifiNetwork.MeteredOverride", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Int) {
        val override =
            WifiNetwork.MeteredOverride.fromValue(value) ?: WifiNetwork.MeteredOverride.NONE
        encoder.encodeString(override.name)
    }

    override fun deserialize(decoder: Decoder): Int {
        val name = decoder.decodeString()
        val override =
            WifiNetwork.MeteredOverride.entries.find { it.name == name }
                ?: WifiNetwork.MeteredOverride.NONE
        return override.value
    }
}

object RandomizationSerializer : KSerializer<Int> {
    override val descriptor =
        PrimitiveSerialDescriptor("WifiNetwork.Randomization", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Int) {
        val randomization =
            WifiNetwork.Randomization.fromValue(value) ?: WifiNetwork.Randomization.default
        encoder.encodeString(randomization.name)
    }

    override fun deserialize(decoder: Decoder): Int {
        val name = decoder.decodeString()
        val randomization =
            WifiNetwork.Randomization.entries
                .find { it.name == name }
                ?.takeIf { it in WifiNetwork.Randomization.supported }
                ?: WifiNetwork.Randomization.default
        return randomization.value
    }
}
