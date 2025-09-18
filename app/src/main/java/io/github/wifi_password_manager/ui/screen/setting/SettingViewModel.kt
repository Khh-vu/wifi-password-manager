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
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.data.Settings
import io.github.wifi_password_manager.services.SettingService
import io.github.wifi_password_manager.services.WifiService
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerializationException

class SettingViewModel(
    private val settingService: SettingService,
    private val wifiService: WifiService,
) : ViewModel() {
    companion object {
        private const val TAG = "SettingViewModel"
    }

    data class State(
        val settings: Settings = Settings(),
        val isLoading: Boolean = false,
        val showForgetAllDialog: Boolean = false,
    )

    sealed interface Action {
        data class UpdateThemeMode(val themeMode: Settings.ThemeMode) : Action

        data class ToggleMaterialYou(val value: Boolean) : Action

        data object ImportNetworks : Action

        data object ExportNetworks : Action

        data object ShowForgetAllDialog : Action

        data object HideForgetAllDialog : Action

        data object ConfirmForgetAllNetworks : Action
    }

    sealed interface Event {
        data class ShowMessage(val messageRes: Int) : Event
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

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    fun onAction(action: Action) {
        Log.d(TAG, "onAction: $action")
        viewModelScope.launch {
            when (action) {
                is Action.UpdateThemeMode ->
                    settingService.updateSettings { it.copy(themeMode = action.themeMode) }
                is Action.ToggleMaterialYou ->
                    settingService.updateSettings { it.copy(useMaterialYou = action.value) }
                is Action.ImportNetworks -> importNetworks()
                is Action.ExportNetworks -> exportNetworks()
                is Action.ShowForgetAllDialog -> showForgetAllDialog()
                is Action.HideForgetAllDialog ->
                    _state.update { it.copy(showForgetAllDialog = false) }
                is Action.ConfirmForgetAllNetworks -> forgetAllNetworks()
            }
        }
    }

    @OptIn(ExperimentalTime::class, FormatStringsInDatetimeFormats::class)
    private suspend fun exportNetworks() {
        val networks = wifiService.getPrivilegedConfiguredNetworks()
        if (networks.isEmpty()) {
            _event.emit(Event.ShowMessage(R.string.no_network_to_export))
            return
        }

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val formatter = LocalDateTime.Format { byUnicodePattern(pattern = "yyyy-MM-dd_HH:mm:ss") }
        val file =
            FileKit.openFileSaver(
                suggestedName = "WiFi_${formatter.format(now)}",
                extension = "json",
            ) ?: return
        Dispatchers.IO { file.writeString(wifiService.exportToJson()) }
        _event.emit(Event.ShowMessage(R.string.export_networks_success))
    }

    private suspend fun importNetworks() {
        val file = FileKit.openFilePicker(type = FileKitType.File("json")) ?: return

        _state.update { it.copy(isLoading = true) }

        try {
            Dispatchers.IO {
                val networks = wifiService.getNetworks(file.readString())
                if (networks.isNotEmpty()) {
                    wifiService.addOrUpdateNetworks(networks)
                }
            }
        } catch (e: SerializationException) {
            Log.e(TAG, "Error parsing JSON", e)

            _state.update { it.copy(isLoading = false) }
            _event.emit(Event.ShowMessage(R.string.invalid_json))

            return
        }

        _state.update { it.copy(isLoading = false) }
        _event.emit(Event.ShowMessage(R.string.import_networks_success))
    }

    private suspend fun showForgetAllDialog() {
        val networks = wifiService.getPrivilegedConfiguredNetworks()
        if (networks.isEmpty()) {
            _event.emit(Event.ShowMessage(R.string.no_network_to_forget))
            return
        }

        _state.update { it.copy(showForgetAllDialog = true) }
    }

    private suspend fun forgetAllNetworks() {
        _state.update { it.copy(isLoading = true, showForgetAllDialog = false) }

        Dispatchers.IO { wifiService.removeAllNetworks() }

        _state.update { it.copy(isLoading = false) }
        _event.emit(Event.ShowMessage(R.string.forget_success))
    }
}
