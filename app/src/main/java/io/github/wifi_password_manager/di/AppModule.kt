package io.github.wifi_password_manager.di

import android.content.Context
import io.github.wifi_password_manager.services.SettingService
import io.github.wifi_password_manager.services.WifiService
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.screen.setting.SettingViewModel
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

    @Single fun wifiService(context: Context, json: Json): WifiService = WifiService(context, json)

    @Single
    fun settingService(context: Context, json: Json): SettingService = SettingService(context, json)

    @KoinViewModel
    fun networkListViewModel(wifiService: WifiService): NetworkListViewModel =
        NetworkListViewModel(wifiService)

    @KoinViewModel
    fun settingViewModel(
        settingService: SettingService,
        wifiService: WifiService,
    ): SettingViewModel = SettingViewModel(settingService, wifiService)
}
