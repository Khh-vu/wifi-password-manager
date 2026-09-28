package io.github.wifi_password_manager.utils

import android.net.wifi.IActionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun awaitAction(block: suspend (IActionListener) -> Unit): Boolean {
    return suspendCancellableCoroutine { continuation ->
        CoroutineScope(continuation.context).launch {
            val listener = object : IActionListener.Stub() {
                override fun onSuccess() {
                    if (continuation.isActive) continuation.resume(true)
                }

                override fun onFailure(reason: Int) {
                    if (continuation.isActive) continuation.resume(false)
                }
            }
            block(listener)
        }
    }
}
