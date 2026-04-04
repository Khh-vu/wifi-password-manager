package io.github.wifi_password_manager.di

import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.repository.FileRepository
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.ui.screen.network.list.NetworkListViewModel
import io.github.wifi_password_manager.ui.screen.note.NoteViewModel
import io.github.wifi_password_manager.ui.screen.setting.SettingViewModel
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
@Configuration
class ViewModelModule {
    @KoinViewModel
    fun networkListViewModel(wifiRepository: WifiRepository) = NetworkListViewModel(wifiRepository)

    @KoinViewModel
    fun noteViewModel(wifiRepository: WifiRepository, @InjectedParam network: WifiNetwork) =
        NoteViewModel(wifiRepository, network)

    @KoinViewModel
    fun settingViewModel(
        settingRepository: SettingRepository,
        wifiRepository: WifiRepository,
        fileRepository: FileRepository,
    ) = SettingViewModel(settingRepository, wifiRepository, fileRepository)
}
