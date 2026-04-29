package io.github.wifi_password_manager.di

import android.content.Context
import androidx.work.WorkerParameters
import io.github.wifi_password_manager.workers.PersistEphemeralNetworksWorker
import org.koin.android.annotation.KoinWorker
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@Configuration
class WorkerModule {
    @KoinWorker
    fun persistEphemeralNetworksWorker(context: Context, params: WorkerParameters) =
        PersistEphemeralNetworksWorker(context, params)
}
