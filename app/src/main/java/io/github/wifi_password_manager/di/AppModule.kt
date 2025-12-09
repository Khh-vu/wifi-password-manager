package io.github.wifi_password_manager.di

import android.content.Context
import androidx.work.WorkerParameters
import io.github.wifi_password_manager.services.FileService
import io.github.wifi_password_manager.services.SettingService
import io.github.wifi_password_manager.services.WifiService
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.screen.setting.SettingViewModel
import io.github.wifi_password_manager.workers.PersistEphemeralNetworksWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.android.annotation.KoinViewModel
import org.koin.android.annotation.KoinWorker
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

    @Single fun wifiService(context: Context): WifiService = WifiService(context)

    @Single
    fun settingService(context: Context, json: Json): SettingService = SettingService(context, json)

    @Single fun fileService(json: Json): FileService = FileService(json, Dispatchers.Default)

    @KoinViewModel
    fun networkListViewModel(wifiService: WifiService): NetworkListViewModel =
        NetworkListViewModel(wifiService)

    @KoinViewModel
    fun settingViewModel(
        settingService: SettingService,
        wifiService: WifiService,
        fileService: FileService,
    ): SettingViewModel = SettingViewModel(settingService, wifiService, fileService)

    @KoinWorker
    fun persistEphemeralNetworksWorker(
        context: Context,
        params: WorkerParameters,
        wifiService: WifiService,
    ): PersistEphemeralNetworksWorker = PersistEphemeralNetworksWorker(context, params, wifiService)
}
