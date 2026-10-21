package io.github.wifi_password_manager.utils

import android.net.wifi.IActionListener
import android.util.Log
import kotlinx.coroutines.CompletableDeferred

suspend fun awaitAction(block: suspend (IActionListener) -> Unit): Boolean {
    val result = CompletableDeferred<Boolean>()
    val listener = object : IActionListener.Stub() {
        override fun onSuccess() {
            result.complete(true)
        }

        override fun onFailure(reason: Int) {
            result.complete(false)
        }
    }
    return try {
        block(listener)
        result.await()
    } catch (e: Exception) {
        Log.e("ActionListener", "Action failed", e)
        return false
    }
}
