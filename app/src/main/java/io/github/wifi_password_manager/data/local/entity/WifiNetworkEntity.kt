package io.github.wifi_password_manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.wifi_password_manager.domain.model.WifiNetwork

@Entity(tableName = "wifi_networks")
data class WifiNetworkEntity(
    @PrimaryKey(autoGenerate = true) val rowid: Int = 0,
    val ssid: String,
    val networkId: Int,
    val securityTypes: String,
    val password: String,
    val hidden: Boolean,
    val autojoin: Boolean,
    val private: Boolean,
    val note: String?,
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
    )
}

fun WifiNetworkEntity.toDomain(): WifiNetwork {
    return WifiNetwork(
        networkId = networkId,
        ssid = ssid,
        securityType =
            securityTypes.split(",").map(WifiNetwork.SecurityType::valueOf).toSet(),
        password = password,
        hidden = hidden,
        autojoin = autojoin,
        private = private,
        note = note,
    )
}
