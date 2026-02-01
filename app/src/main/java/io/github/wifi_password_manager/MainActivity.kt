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
import com.topjohnwu.superuser.Shell
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.navigation.NavigationRoot
import io.github.wifi_password_manager.ui.screen.lock.LockView
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import io.github.wifi_password_manager.utils.isBiometricAuthenticationSupported
import io.github.wifi_password_manager.utils.toast
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val settingRepository by inject<SettingRepository>()
    private var isAuthenticated by mutableStateOf(false)
    private var isRoot by mutableStateOf<Boolean?>(null)

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
                        LockView(onAuthenticated = { isAuthenticated = true })
                    }
                    isRoot == true -> {
                        NavigationRoot()
                    }
                    isRoot == false -> {
                        ShizukuPermissionHandler { NavigationRoot() }
                    }
                }
            }
        }
    }

    private fun setupSplashScreen() {
        var keepSplashScreenOn = true
        installSplashScreen().apply { setKeepOnScreenCondition { keepSplashScreenOn } }

        lifecycleScope.launch {
            val settings = settingRepository.settings.value

            if (settings.appLockEnabled && !isBiometricAuthenticationSupported()) {
                settingRepository.updateSettings { it.copy(appLockEnabled = false) }
                toast(R.string.app_lock_disabled)
            }

            isAuthenticated = !settingRepository.settings.value.appLockEnabled

            isRoot =
                runCatching {
                        withContext(Shell.EXECUTOR.asCoroutineDispatcher()) {
                            Shell.getShell().isRoot
                        }
                    }
                    .getOrElse { false }

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
                        val localeList =
                            LocaleListCompat.forLanguageTags(
                                if ('-' in language.code) language.code.split("-").first()
                                else language.code
                            )
                        AppCompatDelegate.setApplicationLocales(localeList)
                    }
            }
        }
    }
}
