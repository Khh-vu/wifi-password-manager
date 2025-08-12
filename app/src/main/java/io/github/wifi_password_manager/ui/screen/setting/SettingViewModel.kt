package io.github.wifi_password_manager.ui.screen.setting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import io.github.wifi_password_manager.data.Settings
import io.github.wifi_password_manager.services.SettingService
import io.github.wifi_password_manager.services.WifiService
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime

class SettingViewModel(
    private val settingService: SettingService,
    private val wifiService: WifiService,
) : ViewModel() {
    companion object {
        private const val TAG = "SettingViewModel"
    }

    data class State(val settings: Settings = Settings(), val isLoading: Boolean = false)

    sealed interface Event {
        data class UpdateThemeMode(val themeMode: Settings.ThemeMode) : Event

        data class ToggleMaterialYou(val value: Boolean) : Event

        data object ImportNetworks : Event

        data object ExportNetworks : Event

        data object ForgetAllNetworks : Event
    }

    private val _state = MutableStateFlow(State())
    val state =
        combine(_state, settingService.settings) { state, settings ->
                state.copy(settings = settings)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5.seconds),
                initialValue = State(),
            )

    fun onEvent(event: Event) {
        Log.d(TAG, "onEvent: $event")
        viewModelScope.launch {
            when (event) {
                is Event.UpdateThemeMode ->
                    settingService.updateSettings { it.copy(themeMode = event.themeMode) }
                is Event.ToggleMaterialYou ->
                    settingService.updateSettings { it.copy(useMaterialYou = event.value) }
                is Event.ImportNetworks -> importNetworks()
                is Event.ExportNetworks -> exportNetworks()
                is Event.ForgetAllNetworks -> forgetAllNetworks()
            }
        }
    }

    @OptIn(ExperimentalTime::class, FormatStringsInDatetimeFormats::class)
    private suspend fun exportNetworks() {
        val networks = withContext(Dispatchers.IO) { wifiService.getPrivilegedConfiguredNetworks() }
        if (networks.isEmpty()) return

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val formatter = LocalDateTime.Format { byUnicodePattern(pattern = "yyyy-MM-dd_HH:mm:ss") }
        val file =
            FileKit.openFileSaver(
                suggestedName = "WiFi_${formatter.format(now)}",
                extension = "json",
            ) ?: return
        withContext(Dispatchers.IO) { file.writeString(wifiService.exportToJson(networks)) }
    }

    private suspend fun importNetworks() {
        val file = FileKit.openFilePicker(type = FileKitType.File("json")) ?: return

        _state.update { it.copy(isLoading = true) }

        withContext(Dispatchers.IO) {
            val networks = wifiService.getNetworks(file.readString())
            if (networks.isNotEmpty()) {
                wifiService.addOrUpdateNetworks(networks)
            }
        }

        _state.update { it.copy(isLoading = false) }
    }

    private suspend fun forgetAllNetworks() {
        _state.update { it.copy(isLoading = true) }

        withContext(Dispatchers.IO) { wifiService.removeAllNetworks() }

        _state.update { it.copy(isLoading = false) }
    }
}
