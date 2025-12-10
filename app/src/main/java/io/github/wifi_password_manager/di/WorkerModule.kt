package io.github.wifi_password_manager.di

import android.content.Context
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.workers.PersistEphemeralNetworksWorker
import org.koin.android.annotation.KoinWorker
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class WorkerModule {
    @Single fun workManager(context: Context) = WorkManager.getInstance(context)

    @KoinWorker
    fun persistEphemeralNetworksWorker(
        context: Context,
        params: WorkerParameters,
        wifiRepository: WifiRepository,
    ) = PersistEphemeralNetworksWorker(context, params, wifiRepository)
}
