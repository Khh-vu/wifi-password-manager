@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.services

import android.content.AttributionSource
import android.content.AttributionSourceHidden
import android.content.Context
import android.net.wifi.IWifiManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import dev.rikka.tools.refine.Refine
import io.github.wifi_password_manager.data.WifiNetwork
import io.github.wifi_password_manager.utils.fromWifiConfiguration
import io.github.wifi_password_manager.utils.groupAndSortedBySsid
import io.github.wifi_password_manager.utils.hasShizukuPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

private data class ShellResult(val resultCode: Int, val output: String)

class WifiService(private val context: Context, private val json: Json) {
    companion object {
        private const val TAG = "WifiService"

        private const val SHELL_PACKAGE = "com.android.shell"
    }

    private val _configuredNetworks = MutableSharedFlow<List<WifiNetwork>>()
    val configuredNetworks = _configuredNetworks.asSharedFlow()

    private val wifiManager by lazy {
        SystemServiceHelper.getSystemService(Context.WIFI_SERVICE)
            .let(::ShizukuBinderWrapper)
            .let(IWifiManager.Stub::asInterface)
    }

    private val user
        get() =
            when (Shizuku.getUid()) {
                0 -> "root"
                1000 -> "system"
                2000 -> "shell"
                else -> throw IllegalArgumentException("Unknown Shizuku user ${Shizuku.getUid()}")
            }

    private val attributionSource: AttributionSource? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Refine.unsafeCast(
                AttributionSourceHidden(
                    Shizuku.getUid(),
                    SHELL_PACKAGE,
                    SHELL_PACKAGE,
                    null as Set<String>?,
                    null,
                )
            )
        } else {
            null
        }
    }

    suspend fun refresh() {
        val networks = getPrivilegedConfiguredNetworks()
        _configuredNetworks.emit(value = networks)
    }

    fun getPrivilegedConfiguredNetworks(): List<WifiNetwork> {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, returning empty list")
            return emptyList()
        }
        return runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    wifiManager
                        .getPrivilegedConfiguredNetworks(
                            user,
                            SHELL_PACKAGE,
                            Bundle().apply {
                                putParcelable(
                                    "EXTRA_PARAM_KEY_ATTRIBUTION_SOURCE",
                                    attributionSource,
                                )
                            },
                        )
                        ?.list
                } else {
                    wifiManager.getPrivilegedConfiguredNetworks(user, SHELL_PACKAGE)?.list
                }
            }
            .fold(
                onSuccess = { configurations ->
                    configurations
                        ?.map(WifiNetwork::fromWifiConfiguration)
                        .orEmpty()
                        .groupAndSortedBySsid()
                },
                onFailure = {
                    Log.e(TAG, "Error getting configured networks", it)
                    emptyList()
                },
            )
    }

    suspend fun addOrUpdateNetworks(networks: List<WifiNetwork>) {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, cannot add/update networks")
            return
        }

        if (networks.isEmpty()) return

        runCatching {
                withContext(Dispatchers.IO) {
                    val jobs =
                        networks.flatMap { network ->
                            network.securityType.map { securityType ->
                                async {
                                    val command = buildCommand(network, securityType)
                                    execute(command)
                                }
                            }
                        }
                    jobs.awaitAll()
                }
            }
            .fold(
                onSuccess = {
                    refresh()
                    Log.d(TAG, "Networks added or updated")
                },
                onFailure = { Log.e(TAG, "Error adding or updating networks", it) },
            )
    }

    suspend fun removeAllNetworks() {
        if (!context.hasShizukuPermission) {
            Log.w(TAG, "Shizuku permission not available, cannot remove networks")
            return
        }

        val networks = getPrivilegedConfiguredNetworks()
        val validNetworks = networks.filter { it.networkId != -1 }

        if (validNetworks.isEmpty()) {
            Log.d(TAG, "No networks to remove")
            return
        }

        runCatching {
                validNetworks.forEach { network ->
                    wifiManager.removeNetwork(network.networkId, SHELL_PACKAGE)
                }
            }
            .fold(
                onSuccess = {
                    refresh()
                    Log.d(TAG, "All networks removed: ${validNetworks.size} networks")
                },
                onFailure = { Log.e(TAG, "Error removing all networks", it) },
            )
    }

    fun exportToJson(networks: List<WifiNetwork>): String {
        return json.encodeToString(networks)
    }

    fun getNetworks(jsonString: String): List<WifiNetwork> {
        return json.decodeFromString(jsonString)
    }

    private fun buildCommand(network: WifiNetwork, securityType: WifiNetwork.SecurityType): String {
        return buildString {
            append("cmd wifi add-network \"${network.ssid}\"")
            append(
                when (securityType) {
                    WifiNetwork.SecurityType.OPEN -> " open"
                    WifiNetwork.SecurityType.OWE -> " owe"
                    WifiNetwork.SecurityType.WPA2 -> " wpa2 \"${network.password}\""
                    WifiNetwork.SecurityType.WPA3 -> " wpa3 \"${network.password}\""
                    WifiNetwork.SecurityType.WEP -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            " wep \"${network.password}\""
                        } else {
                            throw UnsupportedOperationException(
                                "WEP is not supported on this API level"
                            )
                        }
                    }
                }
            )
            if (!network.autojoin) append(" -d")
            if (network.hidden) append(" -h")
        }
    }

    private fun execute(command: String): ShellResult =
        runCatching {
                val process =
                    IShizukuService.Stub.asInterface(Shizuku.getBinder())
                        .newProcess(arrayOf("sh", "-c", command), null, null)
                val output = process.inputStream.text.ifBlank { process.errorStream.text }
                val resultCode = process.waitFor()
                ShellResult(resultCode = resultCode, output = output.trim())
            }
            .getOrElse { ShellResult(resultCode = -1, output = it.stackTraceToString()) }

    private val ParcelFileDescriptor.text
        get() =
            ParcelFileDescriptor.AutoCloseInputStream(this).use { stream ->
                stream.bufferedReader().use { it.readText() }
            }
}
