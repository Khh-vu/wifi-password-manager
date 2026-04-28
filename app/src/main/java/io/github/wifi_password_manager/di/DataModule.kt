package io.github.wifi_password_manager.di

import android.content.Context
import androidx.room.Room
import io.github.wifi_password_manager.data.local.AppDatabase
import io.github.wifi_password_manager.data.local.dao.WifiNetworkDao
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class DataModule {
    @Single
    fun appDatabase(context: Context): AppDatabase =
        Room.databaseBuilder<AppDatabase>(
                context = context.applicationContext,
                name = context.getDatabasePath(AppDatabase.DATABASE_NAME).absolutePath,
            )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration(false)
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    @Single fun wifiNetworkDao(database: AppDatabase): WifiNetworkDao = database.wifiNetworkDao()
}
