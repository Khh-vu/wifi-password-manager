package io.github.wifi_password_manager.ui.screen.integration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import io.github.wifi_password_manager.BuildConfig
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.receivers.ExportWifiReceiver
import io.github.wifi_password_manager.ui.screen.integration.components.SetupInfoField
import io.github.wifi_password_manager.ui.shared.BackButton
import io.github.wifi_password_manager.ui.theme.ThemeWrapper
import io.github.wifi_password_manager.utils.plus

@Composable
fun ExportWifiSetupView() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton() },
                title = { Text(text = stringResource(R.string.export_wifi_setup_title)) },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = innerPadding + PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Action
            item {
                SetupInfoField(label = "Action", content = ExportWifiReceiver.ACTION_EXPORT_WIFI)
            }

            // Extras
            item {
                SetupInfoField(
                    label = "Extra",
                    content = ExportWifiReceiver.EXTRA_FORMAT,
                    hint = stringResource(R.string.export_wifi_extra_format_hint),
                )
            }
            item {
                SetupInfoField(
                    label = "Extra",
                    content = ExportWifiReceiver.EXTRA_PASSWORD,
                    hint = stringResource(R.string.export_wifi_extra_password_hint),
                )
            }
            item {
                SetupInfoField(
                    label = "Extra",
                    content = ExportWifiReceiver.EXTRA_FILE_NAME,
                    hint = stringResource(R.string.export_wifi_extra_filename_hint),
                )
            }

            // Package
            item { SetupInfoField(label = "Package", content = BuildConfig.APPLICATION_ID) }

            // Class
            item { SetupInfoField(label = "Class", content = ExportWifiReceiver::class.java.name) }
        }
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun ExportWifiSetupViewPreview() {
    ExportWifiSetupView()
}
