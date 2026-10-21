package io.github.wifi_password_manager.di

import android.content.Context
import androidx.room3.Room
import io.github.wifi_password_manager.data.local.AppDatabase
import io.github.wifi_password_manager.data.local.dao.WifiNetworkDao
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class DataModule {
    @Single
    fun appDatabase(context: Context): AppDatabase =
        Room.databaseBuilder<AppDatabase>(
                name = context.getDatabasePath(AppDatabase.DATABASE_NAME).absolutePath
            )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

    @Single fun wifiNetworkDao(database: AppDatabase): WifiNetworkDao = database.wifiNetworkDao()
}
