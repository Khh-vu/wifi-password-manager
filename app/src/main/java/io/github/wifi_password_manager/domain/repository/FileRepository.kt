package io.github.wifi_password_manager.domain.repository

import io.github.wifi_password_manager.domain.model.WifiNetwork

interface FileRepository {
    suspend fun networksToJson(networks: List<WifiNetwork>): String

    suspend fun networksFromJson(jsonString: String): List<WifiNetwork>
}
