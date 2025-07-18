package io.github.wifi_password_manager.utils

fun String.stripQuotes(): String {
    return if (startsWith("\"") && endsWith("\"")) {
        substring(1 until lastIndex)
    } else {
        this
    }
}
