package io.github.wifi_password_manager.data.local.entity

import androidx.room3.Entity
import androidx.room3.Fts4
import androidx.room3.FtsOptions

@Fts4(contentEntity = WifiNetworkEntity::class, tokenizer = FtsOptions.TOKENIZER_ICU)
@Entity(tableName = "wifi_networks_fts")
data class WifiNetworkFtsEntity(val ssid: String, val note: String)
