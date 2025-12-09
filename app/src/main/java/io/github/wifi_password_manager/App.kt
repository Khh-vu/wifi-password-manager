package io.github.wifi_password_manager

import android.app.Application
import android.content.Context
import io.github.wifi_password_manager.di.KoinApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.ksp.generated.startKoin
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.ShizukuProvider

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        KoinApp.startKoin {
            androidLogger()
            androidContext(this@App)
            workManagerFactory()
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        ShizukuProvider.enableMultiProcessSupport(true)
        HiddenApiBypass.addHiddenApiExemptions("")
    }
}
