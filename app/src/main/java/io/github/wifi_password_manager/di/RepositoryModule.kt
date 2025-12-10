package io.github.wifi_password_manager.di

import android.content.Context
import io.github.wifi_password_manager.data.repository.FileRepositoryImpl
import io.github.wifi_password_manager.data.repository.SettingRepositoryImpl
import io.github.wifi_password_manager.data.repository.WifiRepositoryImpl
import io.github.wifi_password_manager.domain.repository.FileRepository
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.domain.repository.WifiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class RepositoryModule {
    @Single(binds = [WifiRepository::class])
    fun wifiRepository(context: Context) = WifiRepositoryImpl(context)

    @Single(binds = [SettingRepository::class])
    fun settingRepository(context: Context, json: Json) =
        SettingRepositoryImpl(context, json, Dispatchers.IO)

    @Single(binds = [FileRepository::class])
    fun fileRepository(json: Json) = FileRepositoryImpl(json, Dispatchers.Default)
}
