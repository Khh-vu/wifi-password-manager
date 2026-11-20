package io.github.wifi_password_manager.data.local

import androidx.room3.AutoMigration
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.migration.AutoMigrationSpec
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import io.github.wifi_password_manager.data.local.dao.WifiNetworkDao
import io.github.wifi_password_manager.data.local.entity.WifiNetworkEntity
import io.github.wifi_password_manager.data.local.entity.WifiNetworkFtsEntity

@Database(
    entities = [WifiNetworkEntity::class, WifiNetworkFtsEntity::class],
    version = 7,
    autoMigrations =
        [
            AutoMigration(2, 3),
            AutoMigration(3, 4),
            AutoMigration(4, 5),
            AutoMigration(5, 6),
            AutoMigration(6, 7, spec = AppDatabase.RebuildFtsSpec::class),
        ],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wifiNetworkDao(): WifiNetworkDao

    class RebuildFtsSpec : AutoMigrationSpec {
        override suspend fun onPostMigrate(connection: SQLiteConnection) {
            connection.execSQL(
                "INSERT INTO `wifi_networks_fts`(`wifi_networks_fts`) VALUES('rebuild')",
            )
        }
    }

    companion object {
        const val DATABASE_NAME = "app.db"

        val MIGRATION_1_2 =
            Migration(1, 2) { connection ->
                connection.apply {
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
                            .trimIndent(),
                    )
                    execSQL(
                        """
                        INSERT OR IGNORE INTO `wifi_networks_new`
                        SELECT ssid, networkId, securityTypes, password, hidden, autojoin, private, note
                        FROM `wifi_networks`
                        """
                            .trimIndent(),
                    )
                    execSQL("DROP TABLE `wifi_networks`")
                    execSQL("ALTER TABLE `wifi_networks_new` RENAME TO `wifi_networks`")
                }
            }
    }
}
