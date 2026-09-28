package io.github.wifi_password_manager.data.datasource.wifi

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.wifi.IActionListener
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.os.IBinder
import android.util.Log
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import io.github.wifi_password_manager.IWifiRootService
import io.github.wifi_password_manager.ipc.WifiNetworkParcel
import io.github.wifi_password_manager.services.WiFiRootService
import io.github.wifi_password_manager.utils.awaitAction
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

class RootWifiDataSourceImpl(context: Context) : WifiDataSource {
    companion object {
        private const val TAG = "RootWifiDataSource"
    }

    private val mutex = Mutex()
    private var cachedService: IWifiRootService? = null
    private val rootServiceIntent by lazy { Intent(context, WiFiRootService::class.java) }

    private suspend fun getService(): IWifiRootService? = mutex.withLock {
        cachedService?.let { service ->
            if (service.asBinder().isBinderAlive) {
                return@withLock service
            } else {
                cachedService = null
            }
        }

        suspendCancellableCoroutine<IWifiRootService?> { continuation ->
            Shell.getShell { shell ->
                if (!shell.isRoot) {
                    Log.w(TAG, "Root permission not available")
                    continuation.resume(null)
                    return@getShell
                }

                val connection = object : ServiceConnection {
                    override fun onServiceConnected(
                        name: ComponentName?,
                        binder: IBinder?,
                    ) {
                        if (binder != null) {
                            val service = IWifiRootService.Stub.asInterface(binder)
                            Log.d(TAG, "WiFiRootService connected")
                            cachedService = service
                            continuation.resume(service)
                        } else {
                            Log.e(TAG, "Received null binder")
                            continuation.resume(null)
                        }
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        Log.d(TAG, "WiFiRootService disconnected")
                        cachedService = null
                    }
                }

                RootService.bind(rootServiceIntent, connection)
                continuation.invokeOnCancellation { RootService.unbind(connection) }
            }
        }
    }

    override suspend fun getPrivilegedConfiguredNetworks(): List<WifiConfiguration> {
        val service = getService()
        if (service == null) {
            Log.w(TAG, "Root service not available, returning empty list")
            return emptyList()
        }
        return service.privilegedConfiguredNetworks.orEmpty().map { it.toWifiConfiguration() }
    }

    override suspend fun addOrUpdateNetworkPrivileged(config: WifiConfiguration): Boolean {
        val service = getService()
        if (service == null) {
            Log.w(TAG, "Root service not available, cannot add/update network")
            return false
        }
        val parcel = WifiNetworkParcel.fromWifiConfiguration(config)
        return service.addOrUpdateNetworkPrivileged(parcel)
    }

    override suspend fun forget(netId: Int): Boolean {
        val service = getService()
        if (service == null) {
            Log.w(TAG, "Root service not available, cannot forget network")
            return false
        }
        return awaitAction { listener -> service.forget(netId, listener) }
    }

    override suspend fun getConnectionInfo(): WifiInfo? {
        val service = getService()
        if (service == null) {
            Log.w(TAG, "Root service not available, cannot get connection info")
            return null
        }
        return service.connectionInfo?.toWifiInfo()
    }

    override suspend fun persistEphemeralNetworks() {
        val service = getService()
        if (service == null) {
            Log.w(TAG, "Root service not available, cannot persist ephemeral networks")
            return
        }
        service.persistEphemeralNetworks()
    }

    override suspend fun disconnect(): Boolean {
        val service = getService()
        if (service == null) {
            Log.w(TAG, "Root service not available, cannot disconnect")
            return false
        }
        return service.disconnect()
    }


    override suspend fun connect(config: WifiConfiguration, listener: IActionListener) {
        val service = getService()
        if (service == null) {
            Log.w(TAG, "Root service not available, cannot connect to network")
            listener.onFailure(0)
            return
        }
        val parcel = WifiNetworkParcel.fromWifiConfiguration(config)
        return service.connect(parcel, listener)
    }
}
