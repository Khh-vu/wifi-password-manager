package io.github.wifi_password_manager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import io.github.wifi_password_manager.domain.model.Settings
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.navigation.NavigationRoot
import io.github.wifi_password_manager.ui.theme.WiFiPasswordManagerTheme
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val settingRepository by inject<SettingRepository>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FileKit.init(this)

        enableEdgeToEdge()
        setupSplashScreen()

        setContent {
            val settings by settingRepository.settings.collectAsStateWithLifecycle(Settings())

            WiFiPasswordManagerTheme(
                darkTheme = settings.themeMode.isDark,
                dynamicColor = settings.useMaterialYou,
            ) {
                ShizukuPermissionHandler(finishCallback = { finish() }) { NavigationRoot() }
            }
        }
    }

    private fun setupSplashScreen() {
        var keepSplashScreenOn = true
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                delay(500.milliseconds)
                keepSplashScreenOn = false
            }
        }

        installSplashScreen().apply { setKeepOnScreenCondition { keepSplashScreenOn } }
    }
}
