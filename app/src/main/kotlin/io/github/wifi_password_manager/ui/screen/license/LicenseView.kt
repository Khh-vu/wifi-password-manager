package io.github.wifi_password_manager.ui.screen.license

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import io.github.wifi_password_manager.R
import io.github.wifi_password_manager.ui.shared.BackButton
import io.github.wifi_password_manager.ui.theme.ThemeWrapper

@Composable
fun LicenseView() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton() },
                title = { Text(text = stringResource(R.string.license_title)) },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        val libraries by produceLibraries(R.raw.aboutlibraries)
        LibrariesContainer(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            libraries = libraries,
            contentPadding = innerPadding,
            licenseDialogConfirmText = stringResource(R.string.ok),
        )
    }
}

@PreviewLightDark
@Composable
@PreviewWrapper(ThemeWrapper::class)
private fun LicenseViewPreview() {
    LicenseView()
}
