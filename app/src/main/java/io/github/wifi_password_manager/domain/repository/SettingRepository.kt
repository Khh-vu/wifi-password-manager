package io.github.wifi_password_manager.domain.repository

import io.github.wifi_password_manager.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingRepository {
    val settings: Flow<Settings>

    suspend fun updateSettings(transform: suspend (Settings) -> Settings)
}
