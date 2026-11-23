package io.github.wifi_password_manager.domain.repository

import io.github.vinceglb.filekit.PlatformFile
import io.github.wifi_password_manager.domain.model.ExportOption
import io.github.wifi_password_manager.domain.model.WifiNetwork

interface FileRepository {
    suspend fun encodeNetworks(
        networks: List<WifiNetwork>,
        option: ExportOption,
        password: String?,
    ): ByteArray

    suspend fun readNetworks(files: List<PlatformFile>, password: String?): List<WifiNetwork>
}
