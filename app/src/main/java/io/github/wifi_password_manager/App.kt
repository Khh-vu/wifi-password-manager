package io.github.wifi_password_manager

import android.app.Application
import io.github.wifi_password_manager.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androix.startup.KoinStartup
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.koinConfiguration
import org.koin.ksp.generated.module
import org.lsposed.hiddenapibypass.HiddenApiBypass

@OptIn(KoinExperimentalAPI::class)
class App : Application(), KoinStartup {
    override fun onCreate() {
        super.onCreate()
        HiddenApiBypass.setHiddenApiExemptions("")
    }

    override fun onKoinStartup(): KoinConfiguration = koinConfiguration {
        androidLogger()
        androidContext(this@App)
        modules(AppModule().module)
    }
}
