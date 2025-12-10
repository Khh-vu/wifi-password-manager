package io.github.wifi_password_manager.services

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.repository.SettingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AutoPersistTileService : TileService() {
    private val settingRepository by inject<SettingRepository>()

    private var listeningJob: Job? = null
    private var coroutineScope: CoroutineScope? = null

    override fun onCreate() {
        super.onCreate()
        coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope?.cancel()
        coroutineScope = null
    }

    override fun onStartListening() {
        super.onStartListening()
        listeningJob =
            coroutineScope?.launch {
                settingRepository.settings
                    .map { it.autoPersistEphemeralNetworks }
                    .distinctUntilChanged()
                    .collect(::updateTileState)
            }
    }

    override fun onStopListening() {
        super.onStopListening()
        listeningJob?.cancel()
        listeningJob = null
    }

    override fun onClick() {
        super.onClick()
        coroutineScope?.launch {
            settingRepository.updateSettings {
                it.copy(autoPersistEphemeralNetworks = !it.autoPersistEphemeralNetworks)
            }
        }
    }

    private fun updateTileState(enabled: Boolean) {
        qsTile?.apply {
            state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            subtitle = if (enabled) getString(R.string.on) else getString(R.string.off)
            updateTile()
        }
    }
}
