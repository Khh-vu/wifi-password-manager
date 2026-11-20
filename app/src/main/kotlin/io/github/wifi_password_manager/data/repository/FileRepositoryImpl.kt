package io.github.wifi_password_manager.data.repository

import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.repository.FileRepository
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
    override suspend fun networksToJson(networks: List<WifiNetwork>): String =
        withContext(dispatcher) { json.encodeToString(networks) }

    override suspend fun networksFromJson(jsonString: String): List<WifiNetwork> =
        withContext(dispatcher) { json.decodeFromString(jsonString) }

    override suspend fun networksToGZip(networks: List<WifiNetwork>): ByteArray =
        withContext(dispatcher) {
            ByteArrayOutputStream().use { byteArray ->
                GZIPOutputStream(byteArray).use { gzip -> json.encodeToStream(networks, gzip) }
                byteArray.toByteArray()
            }
        }

    override suspend fun networksFromGZip(data: ByteArray): List<WifiNetwork> =
        withContext(dispatcher) {
            GZIPInputStream(data.inputStream()).use { gzip ->
                json.decodeFromStream<List<WifiNetwork>>(gzip)
            }
        }
}
