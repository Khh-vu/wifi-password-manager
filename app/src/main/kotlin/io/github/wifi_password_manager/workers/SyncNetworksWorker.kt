package io.github.wifi_password_manager.workers

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.wifi_password_manager.domain.repository.WifiRepository
import java.util.concurrent.TimeUnit
import org.koin.core.component.inject

class SyncNetworksWorker(appContext: Context, params: WorkerParameters) :
    PrivilegedWorker(appContext, params) {
    companion object {
        private const val WORK_NAME = "refresh_networks"

        fun schedule(context: Context) {
            val workRequest =
                PeriodicWorkRequestBuilder<SyncNetworksWorker>(
                        repeatInterval = PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                        repeatIntervalTimeUnit = TimeUnit.MILLISECONDS,
                    )
                    .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    uniqueWorkName = WORK_NAME,
                    existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
                    request = workRequest,
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName = WORK_NAME)
        }
    }

    private val wifiRepository by inject<WifiRepository>()

    override suspend fun doPrivilegedWork() {
        wifiRepository.refresh()
    }
}
