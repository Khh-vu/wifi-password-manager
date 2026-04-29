package io.github.wifi_password_manager

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import io.github.wifi_password_manager.manager.PrivilegedManager
import io.github.wifi_password_manager.navigation.NavigationRoot
import io.github.wifi_password_manager.ui.screen.lock.LockView
import io.github.wifi_password_manager.ui.screen.noaccess.NoAccessView
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import io.github.wifi_password_manager.utils.isBiometricAuthenticationSupported
import io.github.wifi_password_manager.utils.toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : AppCompatActivity() {
    private val privilegedManager by inject<PrivilegedManager>()
    private val settingRepository by inject<SettingRepository>()

    private var isAuthenticated by mutableStateOf(false)
    private var privilegedMode by mutableStateOf<PrivilegedMode?>(null)
    private var skipPrivilegedCheck by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FileKit.init(this)

        enableEdgeToEdge()
        setupSplashScreen()
        setupSecureScreen()
        setupLanguage()

        setContent {
            val settings by settingRepository.settings.collectAsStateWithLifecycle()

            WiFiPasswordManagerTheme(
                darkTheme = settings.themeMode.isDark,
                dynamicColor = settings.useMaterialYou,
            ) {
                when {
                    settings.appLockEnabled && !isAuthenticated -> {
                        LockView { isAuthenticated = true }
                    }
                    privilegedMode?.hasPrivilegedAccess == true ||
                        (privilegedMode == PrivilegedMode.NONE && skipPrivilegedCheck) -> {
                        NavigationRoot()
                    }
                    else -> {
                        NoAccessView(
                            allowSkip = settings.allowCacheMode,
                            onAccessGranted = { privilegedMode = privilegedManager.currentMode },
                            onSkip = { skipPrivilegedCheck = true },
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
            privilegedManager.refresh()
            val settings = settingRepository.settings.value

            if (settings.appLockEnabled && !isBiometricAuthenticationSupported()) {
                settingRepository.updateSettings { it.copy(appLockEnabled = false) }
                toast(R.string.app_lock_disabled)
            }

            isAuthenticated = !settingRepository.settings.value.appLockEnabled
            privilegedMode = privilegedManager.currentMode

            delay(500.milliseconds)
            keepSplashScreenOn = false
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
}
