package io.github.wifi_password_manager.workers

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.topjohnwu.superuser.Shell
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.utils.hasShizukuPermission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class PersistEphemeralNetworksWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params), KoinComponent {
    companion object {
        private const val TAG = "PersistEphemeralWorker"
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

            Log.d(TAG, "Scheduled periodic work to persist ephemeral networks")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName = WORK_NAME)
            Log.d(TAG, "Cancelled periodic work to persist ephemeral networks")
        }
    }

    private val wifiRepository by inject<WifiRepository>()

    override suspend fun doWork(): Result {
        if (!applicationContext.hasShizukuPermission && Shell.isAppGrantedRoot() != true) {
            Log.w(TAG, "Privileged permission (root or Shizuku) not available, skipping work")
            return Result.success()
        }

        return try {
            wifiRepository.persistEphemeralNetworks()
            Log.d(TAG, "Work completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during work execution", e)
            Result.retry()
        }
    }
}
