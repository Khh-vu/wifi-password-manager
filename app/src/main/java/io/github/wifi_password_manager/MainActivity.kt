package io.github.wifi_password_manager

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import io.github.wifi_password_manager.domain.model.PrivilegedMode
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.navigation.NavigationRoot
import io.github.wifi_password_manager.ui.screen.lock.LockView
import io.github.wifi_password_manager.ui.screen.noaccess.NoAccessView
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import io.github.wifi_password_manager.utils.isBiometricAuthenticationSupported
import io.github.wifi_password_manager.utils.toast
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MainActivity : AppCompatActivity() {
    private val settingRepository by inject<SettingRepository>()

    private val viewModel by
        viewModel<MainViewModel> { parametersOf(isBiometricAuthenticationSupported()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FileKit.init(this)

        enableEdgeToEdge()
        setupSplashScreen()
        setupSecureScreen()
        setupLanguage()
        observeEvents()

        setContent {
            val settings by settingRepository.settings.collectAsStateWithLifecycle()
            val privilegedMode by viewModel.privilegedMode.collectAsStateWithLifecycle()
            val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
            val skipPrivilegedCheck by viewModel.skipPrivilegedCheck.collectAsStateWithLifecycle()

            WiFiPasswordManagerTheme(
                darkTheme = settings.themeMode.isDark,
                dynamicColor = settings.useMaterialYou,
            ) {
                when {
                    settings.appLockEnabled && !isAuthenticated -> {
                        LockView(viewModel::onAuthenticated)
                    }
                    privilegedMode.hasPrivilegedAccess ||
                        (privilegedMode == PrivilegedMode.NONE && skipPrivilegedCheck) -> {
                        NavigationRoot()
                    }
                    else -> {
                        NoAccessView(
                            allowSkip = settings.allowCacheMode,
                            onSkip = viewModel::onSkipPrivilegedCheck,
                        )
                    }
                }
            }
        }
    }

    private fun setupSplashScreen() {
        var keepSplashScreenOn = true
        installSplashScreen().apply { setKeepOnScreenCondition { keepSplashScreenOn } }

        lifecycleScope.launch {
            viewModel.isAuthenticated.collect {
                keepSplashScreenOn = false
                cancel()
            }
        }
    }

    private fun setupSecureScreen() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingRepository.settings
                    .map { it.secureScreenEnabled }
                    .distinctUntilChanged()
                    .collect { enabled ->
                        if (enabled) {
                            window?.setFlags(
                                WindowManager.LayoutParams.FLAG_SECURE,
                                WindowManager.LayoutParams.FLAG_SECURE,
                            )
                        } else {
                            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        }
                    }
            }
        }
    }

    private fun setupLanguage() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingRepository.settings
                    .map { it.language }
                    .distinctUntilChanged()
                    .collect { language ->
                        val localeList = LocaleListCompat.forLanguageTags(language.code)
                        AppCompatDelegate.setApplicationLocales(localeList)
                    }
            }
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is MainViewModel.Event.ShowMessage -> {
                            toast(event.message.asString(resources))
                        }
                    }
                }
            }
        }
    }
}
