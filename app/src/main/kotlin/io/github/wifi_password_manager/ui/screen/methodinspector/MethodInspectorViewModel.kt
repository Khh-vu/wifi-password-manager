package io.github.wifi_password_manager.ui.screen.methodinspector

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.writeString
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.MethodInspectorExport
import io.github.wifi_password_manager.utils.UiText
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.lsposed.hiddenapibypass.HiddenApiBypass

@OptIn(FlowPreview::class)
class MethodInspectorViewModel<T : Any>(
    private val forClass: KClass<T>,
    private val json: Json,
) : ViewModel() {
    companion object {
        private const val TAG = "MethodInspectorViewModel"
    }

    @Immutable
    data class State(
        val methods: List<Pair<String, String>> = emptyList(),
        val isExporting: Boolean = false,
        val showingSearch: Boolean = false,
    )

    sealed interface Action {
        data object ExportMethods : Action

        data object ToggleSearch : Action
    }

    sealed interface Event {
        data class ShowMessage(val message: UiText) : Event
    }

    private val methods =
        viewModelScope.async(Dispatchers.Default) {
            HiddenApiBypass.getDeclaredMethods(forClass.java).associate {
                it.name to it.toGenericString()
            }
        }

    private val _isExporting = MutableStateFlow(false)
    private val _showingSearch = MutableStateFlow(false)
    private val _methods = snapshotFlow {
        searchState.text
    }
        .debounce(200.milliseconds)
        .distinctUntilChanged()
        .map { searchText ->
            val query = searchText.trim()
            methods
                .await()
                .let {
                    if (query.isBlank()) {
                        it
                    } else {
                        it.filter { (_, signature) -> signature.contains(query, ignoreCase = true) }
                    }
                }
                .toList()
        }

    val state: StateFlow<State> =
        combine(
                _methods,
                _isExporting,
                _showingSearch,
            ) { methods, isExporting, showingSearch ->
                State(
                    methods = methods,
                    isExporting = isExporting,
                    showingSearch = showingSearch,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5.seconds),
                initialValue = State(),
            )
    val searchState = TextFieldState()

    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    fun onAction(action: Action) {
        Log.d(TAG, "onAction: $action")
        when (action) {
            is Action.ExportMethods -> onExportMethods()
            is Action.ToggleSearch -> onToggleSearch()
        }
    }

    private fun onToggleSearch() {
        _showingSearch.update { !it }
        searchState.clearText()
    }

    private fun onExportMethods() {
        viewModelScope.launch {
            val file =
                FileKit.openFileSaver(
                    suggestedName = "${forClass.simpleName}_methods",
                    defaultExtension = "json",
                ) ?: return@launch

            _isExporting.update { true }

            val result = runCatching {
                Dispatchers.IO {
                    val data = MethodInspectorExport(methods = methods.await().values.toList())
                    val jsonString = json.encodeToString(data)
                    file.writeString(jsonString)
                }
            }

            result.fold(
                onSuccess = {
                    _event.send(
                        Event.ShowMessage(UiText.StringResource(R.string.export_methods_success))
                    )
                },
                onFailure = {
                    Log.e(TAG, "Error exporting methods", it)
                    _event.send(
                        Event.ShowMessage(UiText.StringResource(R.string.export_methods_failed))
                    )
                    if (file.exists()) file.delete()
                },
            )

            _isExporting.update { false }
        }
    }
}
