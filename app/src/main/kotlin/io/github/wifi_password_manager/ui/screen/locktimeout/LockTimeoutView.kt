package io.github.wifi_password_manager.ui.screen.locktimeout

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.UiConfig
import io.github.wifi_password_manager.ui.screen.locktimeout.components.CustomLockTimeoutDialog
import io.github.wifi_password_manager.ui.shared.BackButton
import io.github.wifi_password_manager.ui.theme.ThemeWrapper
import io.github.wifi_password_manager.utils.label
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val presets =
    listOf(
        Duration.ZERO,
        30.seconds,
        1.minutes,
        2.minutes,
        5.minutes,
        Duration.INFINITE,
    )

@Composable
fun LockTimeoutView(lockTimeout: Duration, onLockTimeoutChange: (Duration) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showDialog by retain { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton() },
                title = { Text(text = stringResource(R.string.lock_timeout_title)) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            items(presets) { option ->
                ListItem(
                    onClick = { onLockTimeoutChange(option) },
                    leadingContent = {
                        RadioButton(selected = lockTimeout == option, onClick = null)
                    },
                    shapes = UiConfig.listItemShapes(),
                    colors = UiConfig.listItemColors(),
                ) {
                    Text(text = option.label)
                }
            }

            item {
                val isCustom = lockTimeout !in presets
                val supportingContent = @Composable { Text(text = lockTimeout.label) }

                ListItem(
                    onClick = { showDialog = true },
                    leadingContent = { RadioButton(selected = isCustom, onClick = null) },
                    supportingContent = supportingContent.takeIf { isCustom },
                    shapes = UiConfig.listItemShapes(),
                    colors = UiConfig.listItemColors(),
                ) {
                    Text(text = stringResource(R.string.lock_timeout_custom))
                }
            }
        }
    }

    if (showDialog) {
        CustomLockTimeoutDialog(
            initial = lockTimeout,
            onDismiss = { showDialog = false },
            onConfirm = {
                onLockTimeoutChange(it)
                showDialog = false
            },
        )
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun LockTimeoutViewPreview() {
    val units = listOf(DurationUnit.SECONDS, DurationUnit.MINUTES)
    LockTimeoutView(
        lockTimeout =
            if (Random.nextBoolean()) {
                Random.nextInt(1..60).toDuration(units.random())
            } else {
                presets.random()
            },
        onLockTimeoutChange = {},
    )
}
