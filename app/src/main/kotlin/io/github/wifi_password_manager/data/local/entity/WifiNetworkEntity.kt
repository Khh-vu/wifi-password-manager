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
)

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
    )
}
