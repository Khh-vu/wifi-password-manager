package io.github.wifi_password_manager.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.wifi_password_manager.data.local.entity.WifiNetworkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WifiNetworkDao {
    @Query("SELECT * FROM wifi_networks ORDER BY ssid ASC")
    fun getAllNetworks(): Flow<List<WifiNetworkEntity>>

    @Query(
        """
            SELECT wifi_networks.* FROM wifi_networks
            JOIN wifi_networks_fts ON wifi_networks.ssid = wifi_networks_fts.ssid
            WHERE wifi_networks_fts MATCH :query
        """
    )
    fun getAllNetworks(query: String): Flow<List<WifiNetworkEntity>>

    @Query("SELECT * FROM wifi_networks ORDER BY ssid ASC")
    suspend fun getAllNetworksList(): List<WifiNetworkEntity>

    @Upsert suspend fun upsertNetworks(networks: List<WifiNetworkEntity>)

    @Upsert suspend fun upsertNetwork(network: WifiNetworkEntity)

    @Query("DELETE FROM wifi_networks WHERE ssid = :ssid") suspend fun deleteNetwork(ssid: Int)

    @Query("DELETE FROM wifi_networks WHERE ssid NOT IN (:excludingSsids)")
    suspend fun deleteNetworks(excludingSsids: List<String>)

    @Query("DELETE FROM wifi_networks") suspend fun deleteNetworks()

    @Query("SELECT COUNT(*) FROM wifi_networks") suspend fun getNetworkCount(): Int

    @Query("UPDATE wifi_networks SET note = :note WHERE ssid = :ssid")
    suspend fun updateNote(ssid: String, note: String?)
}
