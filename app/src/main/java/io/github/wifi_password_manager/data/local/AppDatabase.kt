package io.github.wifi_password_manager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import io.github.wifi_password_manager.data.local.dao.WifiNetworkDao
import io.github.wifi_password_manager.data.local.entity.WifiNetworkEntity
import io.github.wifi_password_manager.data.local.entity.WifiNetworkFtsEntity

@TypeConverters
@Database(entities = [WifiNetworkEntity::class, WifiNetworkFtsEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wifiNetworkDao(): WifiNetworkDao

    companion object {
        const val DATABASE_NAME = "app.db"

        val MIGRATION_1_2 =
            Migration(1, 2) { db ->
                db.apply {
                    execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `wifi_networks_new` (
                            `ssid` TEXT NOT NULL,
                            `networkId` INTEGER NOT NULL,
                            `securityTypes` TEXT NOT NULL,
                            `password` TEXT NOT NULL,
                            `hidden` INTEGER NOT NULL,
                            `autojoin` INTEGER NOT NULL,
                            `private` INTEGER NOT NULL,
                            `note` TEXT,
                            PRIMARY KEY(`ssid`, `securityTypes`)
                        )
                        """
                            .trimIndent()
                    )
                    execSQL(
                        """
                        INSERT OR IGNORE INTO `wifi_networks_new`
                        SELECT ssid, networkId, securityTypes, password, hidden, autojoin, private, note
                        FROM `wifi_networks`
                        """
                            .trimIndent()
                    )
                    execSQL("DROP TABLE `wifi_networks`")
                    execSQL("ALTER TABLE `wifi_networks_new` RENAME TO `wifi_networks`")
                }
            }
    }
}
