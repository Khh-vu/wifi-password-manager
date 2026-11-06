package io.github.wifi_password_manager.di

import android.content.Context
import androidx.work.WorkerParameters
import io.github.wifi_password_manager.workers.PersistEphemeralNetworksWorker
import io.github.wifi_password_manager.workers.SyncNetworksWorker
import org.koin.android.annotation.KoinWorker
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@Configuration
class WorkerModule {
    @KoinWorker
    fun persistEphemeralNetworksWorker(context: Context, params: WorkerParameters) =
        PersistEphemeralNetworksWorker(context, params)

    @KoinWorker
    fun syncNetworksWorker(context: Context, params: WorkerParameters) =
        SyncNetworksWorker(context, params)
}
