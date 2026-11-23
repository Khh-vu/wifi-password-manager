package io.github.wifi_password_manager.utils

import io.github.wifi_password_manager.domain.model.WifiNetwork
import io.github.wifi_password_manager.domain.model.WifiNetwork.SecurityType
import kotlin.random.Random

val WifiNetwork.Companion.MOCK
    get() =
        List(20) {
            val type = SecurityType.entries.random()
            WifiNetwork(
                networkId = it,
                ssid = "ssid $it",
                password = if (type in SecurityType.withPassword) "password $it" else "",
                securityType = setOf(type),
                hidden = Random.nextBoolean(),
                autojoin = Random.nextBoolean(),
                private = Random.nextBoolean(),
                note = "Note $it".takeIf { Random.nextBoolean() },
                existInSystem = it % 3 != 0,
            )
        }

fun List<WifiNetwork>.groupBySsidAndPassword(): List<WifiNetwork> = groupBy {
    it.ssid to it.password
}
    .values
    .map { rows ->
        rows
            .first()
            .copy(
                securityType = rows.flatMap { it.securityType }.toSet(),
                existInSystem = rows.any { it.existInSystem },
                note = rows.firstNotNullOfOrNull { it.note },
                createdAt = rows.mapNotNull { it.createdAt }.minOrNull(),
                updatedAt = rows.mapNotNull { it.updatedAt }.maxOrNull(),
            )
    }
    .sortedWith(
        compareBy(String.CASE_INSENSITIVE_ORDER, WifiNetwork::ssid).thenByDescending {
            it.existInSystem
        },
    )
