package io.github.wifi_password_manager.di

import io.github.wifi_password_manager.domain.repository.FileRepository
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.screen.setting.SettingViewModel
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@Configuration
class ViewModelModule {
    @KoinViewModel
    fun networkListViewModel(wifiRepository: WifiRepository) = NetworkListViewModel(wifiRepository)

    @KoinViewModel
    fun settingViewModel(
        settingRepository: SettingRepository,
        wifiRepository: WifiRepository,
        fileRepository: FileRepository,
    ) = SettingViewModel(settingRepository, wifiRepository, fileRepository)
}
