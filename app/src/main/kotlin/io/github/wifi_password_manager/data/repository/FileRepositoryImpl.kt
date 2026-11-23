package io.github.wifi_password_manager.data.repository

import android.util.Log
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.wifi_password_manager.domain.model.ExportOption
import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.model.WifiNetwork.SecurityType
import io.github.wifi_password_manager.domain.repository.FileRepository
import io.github.wifi_password_manager.utils.Crypto
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

@OptIn(ExperimentalSerializationApi::class)
class FileRepositoryImpl(private val json: Json, private val dispatcher: CoroutineDispatcher) :
    FileRepository {
    companion object {
        private const val TAG = "FileRepository"
    }

    override suspend fun encodeNetworks(
        networks: List<WifiNetwork>,
        option: ExportOption,
        password: String?,
    ): ByteArray =
        withContext(dispatcher) {
            val data =
                when (option) {
                    ExportOption.PLAIN -> json.encodeToString(networks).toByteArray()
                    ExportOption.COMPRESSED -> networksToGZip(networks)
                }
            if (password.isNullOrEmpty()) data else Crypto.encrypt(data, password, option)
        }

    override suspend fun readNetworks(
        files: List<PlatformFile>,
        password: String?,
    ): List<WifiNetwork> {
        val results = files.map { file ->
            runCatching { readNetworksFromFile(file, password) }
                .onFailure { Log.e(TAG, "Error parsing file: ${file.name}", it) }
        }
        if (results.none { it.isSuccess }) results.first().getOrThrow()
        return results.flatMap { it.getOrDefault(emptyList()) }
    }

    private suspend fun readNetworksFromFile(
        file: PlatformFile,
        password: String?,
    ): List<WifiNetwork> {
        val data = file.readBytes()
        return withContext(dispatcher) {
            val list =
                when (file.extension.lowercase()) {
                    "bin" if password != null -> {
                        val (option, decrypted) = Crypto.decrypt(data, password)
                        when (option) {
                            ExportOption.PLAIN -> json.decodeFromString(String(decrypted))
                            ExportOption.COMPRESSED -> networksFromGZip(decrypted)
                        }
                    }

                    "gz" -> networksFromGZip(data)
                    else -> json.decodeFromString(String(data))
                }
            list.map { network ->
                if (network.password.isNotBlank()) {
                    val type = network.securityType.filter { it !in SecurityType.withoutPassword }
                    network.copy(securityType = type.toSet())
                } else {
                    network
                }
            }
        }
    }

    private fun networksToGZip(networks: List<WifiNetwork>): ByteArray =
        ByteArrayOutputStream().use { byteArray ->
            GZIPOutputStream(byteArray).use { gzip -> json.encodeToStream(networks, gzip) }
            byteArray.toByteArray()
        }

    private fun networksFromGZip(data: ByteArray): List<WifiNetwork> =
        GZIPInputStream(data.inputStream()).use { gzip ->
            json.decodeFromStream<List<WifiNetwork>>(gzip)
        }
}
