package io.github.wifi_password_manager.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.wifi_password_manager.domain.repository.WifiRepository
import java.util.concurrent.TimeUnit
import org.koin.core.component.inject

class PersistEphemeralNetworksWorker(appContext: Context, params: WorkerParameters) :
    PrivilegedWorker(appContext, params) {
    companion object {
        private const val WORK_NAME = "persist_ephemeral_networks"

        fun schedule(context: Context) {
            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()

            val workRequest =
                PeriodicWorkRequestBuilder<PersistEphemeralNetworksWorker>(
                        repeatInterval = PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                        repeatIntervalTimeUnit = TimeUnit.MILLISECONDS,
                    )
                    .setConstraints(constraints)
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
        wifiRepository.persistEphemeralNetworks()
    }
}
