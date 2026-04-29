package io.github.wifi_password_manager.ui.screen.note

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.ui.shared.TooltipIconButton
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import io.github.wifi_password_manager.utils.DeviceConfiguration
import io.github.wifi_password_manager.utils.MOCK
import io.github.wifi_password_manager.utils.plus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NoteView(network: WifiNetwork, state: String, onAction: (NoteViewModel.Action) -> Unit) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    BackHandler { onAction(NoteViewModel.Action.SaveOrDeleteNote) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    TooltipIconButton(
                        onClick = { onAction(NoteViewModel.Action.SaveOrDeleteNote) },
                        painter = painterResource(R.drawable.ic_arrow_back),
                        tooltip = stringResource(R.string.back),
                        positioning = TooltipAnchorPosition.Below,
                    )
                },
                title = { Text(text = stringResource(R.string.note_title, network.ssid)) },
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

        val contentPadding =
            when (deviceConfiguration) {
                DeviceConfiguration.MOBILE_PORTRAIT -> PaddingValues(12.dp)
                DeviceConfiguration.MOBILE_LANDSCAPE ->
                    PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                DeviceConfiguration.TABLET_PORTRAIT -> PaddingValues(24.dp)
                DeviceConfiguration.TABLET_LANDSCAPE ->
                    PaddingValues(horizontal = 48.dp, vertical = 24.dp)
                DeviceConfiguration.DESKTOP -> PaddingValues(horizontal = 64.dp, vertical = 32.dp)
            }

        val maxWidth =
            when (deviceConfiguration) {
                DeviceConfiguration.MOBILE_PORTRAIT,
                DeviceConfiguration.MOBILE_LANDSCAPE ->
                    WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND.dp
                DeviceConfiguration.TABLET_PORTRAIT ->
                    WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp
                DeviceConfiguration.TABLET_LANDSCAPE,
                DeviceConfiguration.DESKTOP -> WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND.dp
            }

        val minLines =
            when (deviceConfiguration) {
                DeviceConfiguration.MOBILE_PORTRAIT,
                DeviceConfiguration.MOBILE_LANDSCAPE -> 3
                else -> 5
            }

        LazyColumn(
            modifier = Modifier.fillMaxSize().imePadding(),
            contentPadding = innerPadding + contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                OutlinedTextField(
                    value = state,
                    onValueChange = { onAction(NoteViewModel.Action.UpdateNote(it)) },
                    modifier =
                        Modifier.widthIn(max = maxWidth)
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                    placeholder = { Text(text = stringResource(R.string.note_hint)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    minLines = minLines,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun NoteViewPreview() {
    WiFiPasswordManagerTheme {
        val network = WifiNetwork.MOCK.random().copy(note = "Sample note")
        NoteView(network = network, state = network.note.orEmpty(), onAction = {})
    }
}

@PreviewLightDark
@Composable
private fun EmptyNoteViewPreview() {
    WiFiPasswordManagerTheme {
        val network = WifiNetwork.MOCK.random().copy(note = null)
        NoteView(network = network, state = "", onAction = {})
    }
}

@PreviewScreenSizes
@Composable
private fun AdaptiveNoteViewPreview() {
    WiFiPasswordManagerTheme {
        NoteView(network = WifiNetwork.MOCK.random(), state = "", onAction = {})
    }
}
