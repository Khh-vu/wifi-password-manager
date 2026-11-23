package io.github.wifi_password_manager.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import io.github.wifi_password_manager.data.local.entity.WifiNetworkEntity
import io.github.wifi_password_manager.data.local.entity.planSync
import kotlinx.coroutines.flow.Flow

@Dao
interface WifiNetworkDao {
    @Query("SELECT * FROM wifi_networks ORDER BY ssid ASC")
    fun getAllNetworks(): Flow<List<WifiNetworkEntity>>

    @Query(
        """
            SELECT * FROM wifi_networks WHERE ssid LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%'
            UNION
            SELECT wifi_networks.* FROM wifi_networks
            JOIN wifi_networks_fts ON wifi_networks.rowid = wifi_networks_fts.rowid
            WHERE wifi_networks_fts MATCH '*' || :query || '*'
        """,
    )
    fun getAllNetworks(query: String): Flow<List<WifiNetworkEntity>>

    @Query("SELECT * FROM wifi_networks ORDER BY ssid ASC")
    suspend fun getAllNetworksList(): List<WifiNetworkEntity>

    @Upsert suspend fun upsertNetworks(networks: List<WifiNetworkEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(networks: List<WifiNetworkEntity>): List<Long>

    @Transaction
    suspend fun importNetworks(networks: List<WifiNetworkEntity>): Boolean {
        val addedAny = insertIfAbsent(networks).any { it != -1L }
        networks
            .filter { it.note != null }
            .distinctBy { it.ssid to it.password }
            .forEach { updateNote(it.ssid, it.password, it.note) }
        return addedAny
    }

    @Delete suspend fun deleteNetworks(networks: List<WifiNetworkEntity>)

    @Transaction
    suspend fun syncNetworks(networks: List<WifiNetworkEntity>) {
        val plan = planSync(getAllNetworksList(), networks, System.currentTimeMillis())
        deleteNetworks(plan.deletes)
        upsertNetworks(plan.upserts)
    }

    @Query(
        """
            UPDATE wifi_networks SET existInSystem = 0
            WHERE ssid = :ssid AND securityTypes = :securityTypes AND existInSystem = 1
        """,
    )
    suspend fun markNotExistInSystem(ssid: String, securityTypes: String)

    @Transaction
    suspend fun markNotExistInSystem(ssidAndSecurityTypes: List<Pair<String, String>>) {
        ssidAndSecurityTypes.forEach { (ssid, securityTypes) ->
            markNotExistInSystem(ssid, securityTypes)
        }
    }

    @Query("UPDATE wifi_networks SET existInSystem = 0") suspend fun markAllNotExistInSystem()

    @Query("DELETE FROM wifi_networks WHERE ssid = :ssid AND password = :password")
    suspend fun deleteNetwork(ssid: String, password: String)

    @Query("DELETE FROM wifi_networks") suspend fun clearNetworks()

    @Query("SELECT COUNT(*) FROM wifi_networks") suspend fun getNetworkCount(): Int

    @Query("UPDATE wifi_networks SET note = :note WHERE ssid = :ssid AND password = :password")
    suspend fun updateNote(ssid: String, password: String, note: String?)
}
