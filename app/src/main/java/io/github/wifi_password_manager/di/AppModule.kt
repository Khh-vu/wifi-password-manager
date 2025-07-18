package io.github.wifi_password_manager.di

import io.github.wifi_password_manager.services.WifiService
import io.github.wifi_password_manager.ui.screen.main.MainViewModel
import kotlinx.serialization.json.Json
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class AppModule {
    @Single
    fun json(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        prettyPrint = true
        coerceInputValues = true
    }

    @Single fun wifiService(json: Json): WifiService = WifiService(json)

    @KoinViewModel
    fun mainViewModel(wifiService: WifiService): MainViewModel = MainViewModel(wifiService)
}
