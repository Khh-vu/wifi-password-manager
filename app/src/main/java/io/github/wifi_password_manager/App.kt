package io.github.wifi_password_manager

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.skydoves.compose.stability.runtime.ComposeStabilityAnalyzer
import com.topjohnwu.superuser.Shell
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.workers.PersistEphemeralNetworksWorker
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.annotation.KoinApplication
import org.koin.ksp.generated.startKoin
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.ShizukuProvider

@KoinApplication
class App : Application() {
    init {
        Shell.enableVerboseLogging = BuildConfig.DEBUG
    }

    private val settingRepository by inject<SettingRepository>()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            workManagerFactory()
        }

        ComposeStabilityAnalyzer.setEnabled(BuildConfig.DEBUG)
        observeAutoPersistEphemeralNetworks()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        ShizukuProvider.enableMultiProcessSupport(true)
        HiddenApiBypass.addHiddenApiExemptions("")
    }

    private fun observeAutoPersistEphemeralNetworks() {
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            settingRepository.settings
                .map { it.autoPersistEphemeralNetworks }
                .distinctUntilChanged()
                .collect { enabled ->
                    if (enabled) {
                        PersistEphemeralNetworksWorker.schedule(this@App)
                    } else {
                        PersistEphemeralNetworksWorker.cancel(this@App)
                    }
                }
        }
    }
}
