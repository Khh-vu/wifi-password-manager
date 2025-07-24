@file:Suppress("DEPRECATION")

package io.github.wifi_password_manager.services

import android.content.AttributionSource
import android.content.Context
import android.net.wifi.IWifiManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import io.github.wifi_password_manager.data.ShellResult
import io.github.wifi_password_manager.data.WifiNetwork
import kotlinx.serialization.json.Json
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

class WifiService(private val json: Json) {
    companion object {
        private const val TAG = "WifiService"

        private const val SHELL_PACKAGE = "com.android.shell"
    }

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

    fun getPrivilegedConfiguredNetworks(): List<WifiNetwork> =
        runCatching {
                val configs =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        wifiManager
                            .getPrivilegedConfiguredNetworks(
                                user,
                                SHELL_PACKAGE,
                                Bundle().apply {
                                    putParcelable(
                                        "EXTRA_PARAM_KEY_ATTRIBUTION_SOURCE",
                                        AttributionSource::class
                                            .java
                                            .getConstructor(
                                                Int::class.java,
                                                String::class.java,
                                                String::class.java,
                                                Set::class.java,
                                                AttributionSource::class.java,
                                            )
                                            .newInstance(
                                                Shizuku.getUid(),
                                                SHELL_PACKAGE,
                                                SHELL_PACKAGE,
                                                null as Set<String>?,
                                                null,
                                            ),
                                    )
                                },
                            )
                            ?.list
                    } else {
                        wifiManager.getPrivilegedConfiguredNetworks(user, SHELL_PACKAGE)?.list
                    }
                configs?.forEach { Log.d(TAG, it.toString()) }
                configs
            }
            .fold(
                onSuccess = { it?.map(WifiNetwork::fromWifiConfiguration).orEmpty() },
                onFailure = {
                    Log.e(TAG, "Error getting configured networks", it)
                    emptyList()
                },
            )

    fun addOrUpdateNetwork(network: WifiNetwork): Boolean =
        runCatching {
                val command = buildString {
                    append("cmd wifi add-network \"${network.ssid}\"")

                    when (network.securityType) {
                        WifiNetwork.SecurityType.OPEN -> append(" open")
                        WifiNetwork.SecurityType.OWE -> append(" owe")
                        WifiNetwork.SecurityType.WPA2 -> append(" wpa2 \"${network.password}\"")
                        WifiNetwork.SecurityType.WPA3 -> append(" wpa3 \"${network.password}\"")
                        WifiNetwork.SecurityType.WEP -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                append(" wep \"${network.password}\"")
                            } else {
                                throw UnsupportedOperationException(
                                    "WEP is not supported on this API level"
                                )
                            }
                        }
                    }

                    if (!network.autojoin) append(" -d")
                    if (network.hidden) append(" -h")
                }
                execute(command)
            }
            .fold(
                onSuccess = {
                    Log.d(TAG, "Network added or updated: $network")
                    it.resultCode != -1
                },
                onFailure = {
                    Log.e(TAG, "Error adding or updating network", it)
                    false
                },
            )

    fun removeNetwork(netId: Int): Boolean =
        runCatching { execute("cmd wifi forget-network $netId") }
            .fold(
                onSuccess = {
                    Log.d(TAG, "Network removed: $netId")
                    it.resultCode != -1
                },
                onFailure = {
                    Log.e(TAG, "Error removing network", it)
                    false
                },
            )

    fun exportToJson(networks: List<WifiNetwork>): String {
        return json.encodeToString(networks)
    }

    fun getNetworks(jsonString: String): List<WifiNetwork> {
        return json.decodeFromString(jsonString)
    }

    private fun execute(command: String): ShellResult =
        runCatching {
                val process =
                    IShizukuService.Stub.asInterface(Shizuku.getBinder())
                        .newProcess(arrayOf("sh", "-c", command), null, null)
                val output = process.inputStream.text.ifBlank { process.errorStream.text }
                val resultCode = process.waitFor()
                ShellResult(resultCode, output.trim())
            }
            .getOrElse { ShellResult(resultCode = -1, output = it.stackTraceToString()) }

    private val ParcelFileDescriptor.text
        get() =
            ParcelFileDescriptor.AutoCloseInputStream(this).use { it.bufferedReader().readText() }
}
