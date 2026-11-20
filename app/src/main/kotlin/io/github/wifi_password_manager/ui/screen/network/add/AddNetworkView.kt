package io.github.wifi_password_manager.ui.screen.network.add

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.ui.UiConfig
import io.github.wifi_password_manager.ui.shared.BackButton
import io.github.wifi_password_manager.ui.shared.PasswordTextField
import io.github.wifi_password_manager.ui.theme.ThemeWrapper
import io.github.wifi_password_manager.utils.plus
import kotlin.random.Random

@Composable
fun AddNetworkView(
    state: AddNetworkViewModel.State,
    ssidState: TextFieldState,
    passwordState: TextFieldState,
    noteState: TextFieldState,
    onAction: (AddNetworkViewModel.Action) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val ssid = ssidState.text.toString()
    val password = passwordState.text.toString()

    val ssidError = AddNetworkViewModel.ssidError(ssid)
    val passwordError = AddNetworkViewModel.passwordError(state.security, password)

    val fieldModifier =
        Modifier.widthIn(max = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp).fillMaxWidth()

    Scaffold(
        modifier =
            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).pointerInput(Unit) {
                detectTapGestures {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            },
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton() },
                title = { Text(text = stringResource(R.string.add_network)) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().imePadding(),
            contentPadding = innerPadding + PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.canSaveToDevice) {
                item {
                    Column(
                        modifier = fieldModifier,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.save_to_section),
                            style = MaterialTheme.typography.labelLarge,
                        )

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                        ) {
                            ToggleButton(
                                modifier = Modifier.weight(1f),
                                checked = !state.saveToDevice,
                                onCheckedChange = {
                                    onAction(AddNetworkViewModel.Action.SaveToDeviceToggled(false))
                                },
                                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                            ) {
                                Text(text = stringResource(R.string.import_to_app))
                            }
                            ToggleButton(
                                modifier = Modifier.weight(1f),
                                checked = state.saveToDevice,
                                onCheckedChange = {
                                    onAction(AddNetworkViewModel.Action.SaveToDeviceToggled(true))
                                },
                                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                            ) {
                                Text(text = stringResource(R.string.import_to_device))
                            }
                        }
                    }
                }
            }

            item {
                val showError = ssidError.takeIf { ssid.isNotEmpty() }
                OutlinedTextField(
                    state = ssidState,
                    modifier = fieldModifier,
                    label = { Text(text = stringResource(R.string.ssid)) },
                    isError = showError != null,
                    supportingText = showError?.let { { Text(text = stringResource(it)) } },
                    lineLimits = TextFieldLineLimits.SingleLine,
                )
            }

            item {
                ChipSection(
                    modifier = fieldModifier,
                    title = stringResource(R.string.security_section),
                    options = WifiNetwork.SecurityType.entries,
                    selected = state.security,
                    label = { stringResource(it.labelResId) },
                    onSelect = { onAction(AddNetworkViewModel.Action.SecurityChanged(it)) },
                )
            }

            if (state.security !in WifiNetwork.SecurityType.withoutPassword) {
                item {
                    PasswordTextField(
                        modifier = fieldModifier,
                        state = passwordState,
                        isError = password.isNotEmpty() && passwordError != null,
                        errorText = passwordError?.let { stringResource(it) }.orEmpty(),
                    )
                }
            }

            item {
                ListItem(
                    modifier = fieldModifier,
                    trailingContent = {
                        Switch(
                            checked = state.hidden,
                            onCheckedChange = {
                                onAction(AddNetworkViewModel.Action.HiddenToggled(it))
                            },
                        )
                    },
                    contentPadding = PaddingValues.Zero,
                    shapes = UiConfig.listItemShapes(),
                    colors = UiConfig.listItemColors(),
                ) {
                    Text(text = stringResource(R.string.hidden_network))
                }
            }

            item {
                ChipSection(
                    modifier = fieldModifier,
                    title = stringResource(R.string.metered_section),
                    options = WifiNetwork.MeteredOverride.entries,
                    selected = state.meteredOverride,
                    label = { stringResource(it.labelResId) },
                    onSelect = { onAction(AddNetworkViewModel.Action.MeteredChanged(it)) },
                )
            }

            item {
                ChipSection(
                    modifier = fieldModifier,
                    title = stringResource(R.string.mac_randomization_section),
                    options = WifiNetwork.Randomization.supported,
                    selected = state.macRandomization,
                    label = { stringResource(it.labelResId) },
                    onSelect = { onAction(AddNetworkViewModel.Action.MacRandomizationChanged(it)) },
                )
            }

            item {
                OutlinedTextField(
                    state = noteState,
                    modifier = fieldModifier,
                    label = { Text(text = stringResource(R.string.note_optional)) },
                    lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 2),
                )
            }

            item {
                TextButton(
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End),
                    onClick = { onAction(AddNetworkViewModel.Action.Save) },
                    shapes = ButtonDefaults.shapes(),
                    enabled = ssidError == null && passwordError == null && !state.isSaving,
                ) {
                    Text(text = stringResource(R.string.save))
                }
            }
        }
    }
}

@Composable
private fun <T> ChipSection(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach {
                FilterChip(
                    selected = selected == it,
                    onClick = { onSelect(it) },
                    label = { Text(text = label(it)) },
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun AddNetworkViewPreview() {
    AddNetworkView(
        state =
            AddNetworkViewModel.State(
                security = WifiNetwork.SecurityType.entries.random(),
                canSaveToDevice = true,
                saveToDevice = Random.nextBoolean(),
            ),
        ssidState = rememberTextFieldState("Home WiFi"),
        passwordState = rememberTextFieldState("password"),
        noteState = rememberTextFieldState(),
        onAction = {},
    )
}
