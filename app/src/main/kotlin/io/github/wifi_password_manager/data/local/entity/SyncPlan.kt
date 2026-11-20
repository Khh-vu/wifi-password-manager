package io.github.wifi_password_manager.data.local.entity

data class SyncPlan(val upserts: List<WifiNetworkEntity>, val deletes: List<WifiNetworkEntity>)

fun planSync(
    existing: List<WifiNetworkEntity>,
    system: List<WifiNetworkEntity>,
    timeMillis: Long,
): SyncPlan {
    val byKey = existing.associateBy { it.key }
    val bySecurity =
        existing.filter { it.existInSystem }.associateBy { it.ssid to it.securityTypes }
    val notes =
        existing.filter { it.note != null }.associate { (it.ssid to it.password) to it.note }

    val matched = hashSetOf<Triple<String, String, String>>()
    val deletes = mutableListOf<WifiNetworkEntity>()

    val upserts = system.map { network ->
        val old =
            byKey[network.key]
                ?: bySecurity[network.ssid to network.securityTypes]?.also { deletes += it }
        old?.let { matched += it.key }

        val stamped =
            if (existing.isEmpty()) {
                network
            } else {
                network.withTimestamps(old, timeMillis)
            }
        stamped.copy(note = old?.note ?: notes[network.ssid to network.password])
    }
    val notInSystem =
        existing
            .filter { it.existInSystem && it.key !in matched }
            .map { it.copy(existInSystem = false) }
    return SyncPlan(upserts = upserts + notInSystem, deletes = deletes)
}

private fun WifiNetworkEntity.withTimestamps(
    oldNetwork: WifiNetworkEntity?,
    timeMillis: Long,
): WifiNetworkEntity =
    when {
        oldNetwork == null -> {
            copy(createdAt = timeMillis, updatedAt = timeMillis)
        }
        hasSameSettings(oldNetwork) -> {
            copy(createdAt = oldNetwork.createdAt, updatedAt = oldNetwork.updatedAt)
        }
        else -> {
            copy(createdAt = oldNetwork.createdAt, updatedAt = timeMillis)
        }
    }

private fun WifiNetworkEntity.hasSameSettings(other: WifiNetworkEntity): Boolean =
    password == other.password &&
        hidden == other.hidden &&
        autojoin == other.autojoin &&
        private == other.private &&
        meteredOverride == other.meteredOverride &&
        macRandomizationSetting == other.macRandomizationSetting
