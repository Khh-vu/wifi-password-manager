package io.github.wifi_password_manager.services

import io.github.wifi_password_manager.data.WifiNetwork
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.invoke
import kotlinx.serialization.json.Json

class FileService(private val json: Json, private val dispatcher: CoroutineDispatcher) {
    suspend fun networksToJson(networks: List<WifiNetwork>): String = dispatcher {
        json.encodeToString(networks)
    }

    suspend fun networksFromJson(jsonString: String): List<WifiNetwork> = dispatcher {
        json.decodeFromString(jsonString)
    }
}
