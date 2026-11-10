package io.github.wifi_password_manager.data.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import io.github.wifi_password_manager.domain.model.WifiNetwork

@Entity(tableName = "wifi_networks", primaryKeys = ["ssid", "securityTypes"])
data class WifiNetworkEntity(
    val ssid: String,
    val networkId: Int,
    val securityTypes: String,
    val password: String,
    val hidden: Boolean,
    val autojoin: Boolean,
    val private: Boolean,
    val note: String?,
    @ColumnInfo(defaultValue = "1") val existInSystem: Boolean = true,
    @ColumnInfo(defaultValue = "NONE") val meteredOverride: String = "NONE",
    @ColumnInfo(defaultValue = "AUTO") val macRandomizationSetting: String = "AUTO",
    val randomizedMacAddress: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
)

fun WifiNetworkEntity.withTimestamps(
    oldNetwork: WifiNetworkEntity?,
    timeMillis: Long,
): WifiNetworkEntity =
    when {
        oldNetwork == null -> {
            copy(createdAt = timeMillis, updatedAt = timeMillis)
        }
        hasSameSettings(oldNetwork) -> {
            copy(createdAt = oldNetwork.createdAt, updatedAt = oldNetwork.updatedAt)
        }
        else -> {
            copy(createdAt = oldNetwork.createdAt, updatedAt = timeMillis)
        }
    }

private fun WifiNetworkEntity.hasSameSettings(other: WifiNetworkEntity): Boolean =
    password == other.password &&
        hidden == other.hidden &&
        autojoin == other.autojoin &&
        private == other.private &&
        meteredOverride == other.meteredOverride &&
        macRandomizationSetting == other.macRandomizationSetting

fun WifiNetwork.toEntity(): WifiNetworkEntity {
    return WifiNetworkEntity(
        networkId = networkId,
        ssid = ssid,
        securityTypes = securityType.joinToString(",") { it.name },
        password = password,
        hidden = hidden,
        autojoin = autojoin,
        private = private,
        note = note,
        existInSystem = existInSystem,
        meteredOverride = meteredOverride.name,
        macRandomizationSetting = macRandomizationSetting.name,
        randomizedMacAddress = randomizedMacAddress,
    )
}

fun WifiNetworkEntity.toDomain(): WifiNetwork {
    return WifiNetwork(
        networkId = networkId,
        ssid = ssid,
        securityType = securityTypes.split(",").map(WifiNetwork.SecurityType::valueOf).toSet(),
        password = password,
        hidden = hidden,
        autojoin = autojoin,
        private = private,
        note = note,
        existInSystem = existInSystem,
        meteredOverride = WifiNetwork.MeteredOverride.valueOf(meteredOverride),
        macRandomizationSetting = WifiNetwork.Randomization.valueOf(macRandomizationSetting),
        randomizedMacAddress = randomizedMacAddress,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
