@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.workers

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfigurationHidden
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.rikka.tools.refine.Refine
import io.github.wifi_password_manager.services.WifiService
import io.github.wifi_password_manager.utils.hasShizukuPermission
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.invoke

class PersistEphemeralNetworksWorker(
    appContext: Context,
    params: WorkerParameters,
    private val wifiService: WifiService,
) : CoroutineWorker(appContext, params) {

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

    override suspend fun doWork(): Result {
        if (!applicationContext.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, skipping work")
            return Result.success()
        }

        return try {
            val configs = wifiService.getPrivilegedConfiguredNetworks()
            val ephemeralConfigs =
                configs.filter { Refine.unsafeCast<WifiConfigurationHidden>(it).isEphemeral }
            if (ephemeralConfigs.isEmpty()) {
                Log.d(TAG, "No ephemeral networks found, skipping work")
                return Result.success()
            }

            val persistentConfigs =
                ephemeralConfigs.map { config ->
                    val hiddenConfig = Refine.unsafeCast<WifiConfigurationHidden>(config)
                    hiddenConfig.apply {
                        ephemeral = false
                        fromWifiNetworkSuggestion = false
                    }
                    Refine.unsafeCast<WifiConfiguration>(hiddenConfig)
                }

            Dispatchers.IO {
                persistentConfigs
                    .map { async { wifiService.addOrUpdateNetworkPrivileged(it) } }
                    .awaitAll()
            }

            Log.d(TAG, "Work completed. Persisted ${persistentConfigs.size} ephemeral networks")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during work execution", e)
            Result.retry()
        }
    }
}
