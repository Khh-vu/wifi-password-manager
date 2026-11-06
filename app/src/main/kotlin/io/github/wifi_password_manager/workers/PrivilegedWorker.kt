package io.github.wifi_password_manager.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.wifi_password_manager.manager.PrivilegedManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class PrivilegedWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params), KoinComponent {
    companion object {
        const val TAG = "PrivilegedWorker"
    }

    private val privilegedManager by inject<PrivilegedManager>()

    protected abstract suspend fun doPrivilegedWork()

    override suspend fun doWork(): Result {
        privilegedManager.refresh()
        val mode = privilegedManager.currentMode
        if (!mode.hasPrivilegedAccess) {
            Log.i(TAG, "Privileged permission (${mode.name}) not available, skipping work")
            return Result.success()
        }

        return try {
            doPrivilegedWork()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during work execution", e)
            Result.retry()
        }
    }
}
