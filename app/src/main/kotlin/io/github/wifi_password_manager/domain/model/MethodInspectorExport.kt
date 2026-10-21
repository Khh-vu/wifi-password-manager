package io.github.wifi_password_manager.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MethodInspectorExport(
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val methods: List<String>,
)
