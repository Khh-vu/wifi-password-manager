package io.github.wifi_password_manager.data.repository

import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.repository.FileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.invoke
import kotlinx.serialization.json.Json

class FileRepositoryImpl(private val json: Json, private val dispatcher: CoroutineDispatcher) :
    FileRepository {
    override suspend fun networksToJson(networks: List<WifiNetwork>): String = dispatcher {
        json.encodeToString(networks)
    }

    override suspend fun networksFromJson(jsonString: String): List<WifiNetwork> = dispatcher {
        json.decodeFromString(jsonString)
    }
}
