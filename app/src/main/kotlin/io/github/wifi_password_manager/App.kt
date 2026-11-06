package io.github.wifi_password_manager

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.skydoves.compose.stability.runtime.ComposeStabilityAnalyzer
import com.topjohnwu.superuser.Shell
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.manager.PrivilegedManager
import io.github.wifi_password_manager.utils.setAppTheme
import io.github.wifi_password_manager.workers.PersistEphemeralNetworksWorker
import io.github.wifi_password_manager.workers.SyncNetworksWorker
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.ShizukuProvider

@KoinApplication
class App : Application() {
    companion object {
        private const val TAG = "App"
    }

    init {
        Shell.enableVerboseLogging = BuildConfig.DEBUG
    }

    private val settingRepository by inject<SettingRepository>()
    private val wifiRepository by inject<WifiRepository>()
    private val privilegedManager by inject<PrivilegedManager>()

    override fun onCreate() {
        super.onCreate()

        startKoin<App> {
            if (BuildConfig.DEBUG) androidLogger()
            androidContext(this@App)
            workManagerFactory()
        }

        ComposeStabilityAnalyzer.setEnabled(BuildConfig.DEBUG)
        setAppTheme()
        observeAutoPersistEphemeralNetworks()
        observeAutoSyncNetworks()
        observeConfiguredNetworksChanged()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        ShizukuProvider.enableMultiProcessSupport(true)
        HiddenApiBypass.addHiddenApiExemptions("")
    }

    private fun setAppTheme() {
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            val themeMode = settingRepository.getCurrentSettings().themeMode
            setAppTheme(themeMode)
        }
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

    private fun observeAutoSyncNetworks() {
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            settingRepository.settings
                .map { it.autoSyncNetworks }
                .distinctUntilChanged()
                .collect { enabled ->
                    if (enabled) {
                        SyncNetworksWorker.schedule(this@App)
                    } else {
                        SyncNetworksWorker.cancel(this@App)
                    }
                }
        }
    }

    private fun observeConfiguredNetworksChanged() {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    ProcessLifecycleOwner.get().lifecycleScope.launch {
                        try {
                            privilegedManager.refresh()
                            wifiRepository.refresh()
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to sync changed networks", e)
                        }
                    }
                }
            }
        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter("android.net.wifi.CONFIGURED_NETWORKS_CHANGE"),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }
}
