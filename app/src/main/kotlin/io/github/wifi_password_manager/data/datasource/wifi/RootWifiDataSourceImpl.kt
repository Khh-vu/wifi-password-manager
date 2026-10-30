package io.github.wifi_password_manager.data.datasource.wifi

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import io.github.wifi_password_manager.IWifiRootService
import io.github.wifi_password_manager.ipc.WifiNetworkParcel
import io.github.wifi_password_manager.services.WiFiRootService
import io.github.wifi_password_manager.utils.awaitAction
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class RootWifiDataSourceImpl(context: Context) : WifiDataSource {
    companion object {
        private const val TAG = "RootWifiDataSource"
    }

    private val mutex = Mutex()
    private var cachedService: IWifiRootService? = null
    private val rootServiceIntent by lazy { Intent(context, WiFiRootService::class.java) }

    open fun bind(connection: ServiceConnection, onUnavailable: () -> Unit) {
        Shell.getShell { shell ->
            if (!shell.isRoot) {
                Log.d(TAG, "Root permission not available")
                onUnavailable()
                return@getShell
            }
            RootService.bind(rootServiceIntent, connection)
        }
    }

    open fun unbind(connection: ServiceConnection) {
        RootService.unbind(connection)
    }

    private suspend fun getService(): IWifiRootService? = mutex.withLock {
        cachedService?.let { service ->
            if (service.asBinder().isBinderAlive) {
                return@withLock service
            } else {
                cachedService = null
            }
        }

        suspendCancellableCoroutine<IWifiRootService?> { continuation ->
            val connection =
                object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                        if (binder != null) {
                            val service = IWifiRootService.Stub.asInterface(binder)
                            cachedService = service
                            continuation.resume(service)
                        } else {
                            continuation.resume(null)
                        }
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        cachedService = null
                    }
                }

            bind(connection) { continuation.resume(null) }
            continuation.invokeOnCancellation { unbind(connection) }
        }
    }

    override suspend fun getPrivilegedConfiguredNetworks(): List<WifiConfiguration> {
        val service = getService()
        if (service == null) {
            Log.d(TAG, "Service not available, returning empty list")
            return emptyList()
        }
        return service.privilegedConfiguredNetworks.orEmpty().map { it.toWifiConfiguration() }
    }

    override suspend fun addOrUpdateNetworkPrivileged(config: WifiConfiguration): Int {
        val service = getService()
        if (service == null) {
            Log.d(TAG, "Service not available, cannot add/update network")
            return -1
        }
        val parcel = WifiNetworkParcel.fromWifiConfiguration(config)
        return service.addOrUpdateNetworkPrivileged(parcel)
    }

    override suspend fun forget(netId: Int): Boolean {
        val service = getService()
        if (service == null) {
            Log.d(TAG, "Service not available, cannot forget network")
            return false
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            awaitAction { listener -> service.forget(netId, listener) }
        } else {
            service.removeNetwork(netId)
        }
    }

    override suspend fun getConnectionInfo(): WifiInfo? {
        val service = getService()
        if (service == null) {
            Log.d(TAG, "Service not available, cannot get connection info")
            return null
        }
        return service.connectionInfo?.toWifiInfo()
    }

    override suspend fun persistEphemeralNetworks() {
        val service = getService()
        if (service == null) {
            Log.d(TAG, "Service not available, cannot persist ephemeral networks")
            return
        }
        service.persistEphemeralNetworks()
    }

    override suspend fun disconnect(): Boolean {
        val service = getService()
        if (service == null) {
            Log.d(TAG, "Service not available, cannot disconnect")
            return false
        }
        return service.disconnect()
    }

    override suspend fun connect(config: WifiConfiguration?, netId: Int): Boolean {
        val service = getService()
        if (service == null) {
            Log.d(TAG, "Service not available, cannot connect to network")
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return awaitAction { listener ->
                service.connect(
                    config?.let(WifiNetworkParcel::fromWifiConfiguration),
                    netId,
                    listener,
                )
            }
        }

        val id =
            when {
                netId != -1 -> netId
                config != null -> addOrUpdateNetworkPrivileged(config)
                else -> -1
            }

        return id != -1 && service.enableNetwork(id) && service.reconnect()
    }
}
