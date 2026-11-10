package io.github.wifi_password_manager.domain.model

import androidx.annotation.StringRes
import io.github.wifi_password_manager.R

enum class ExportOption(@StringRes val titleResId: Int, @StringRes val descriptionResId: Int) {
    PLAIN(R.string.export_option_plain, R.string.export_option_plain_description),
    COMPRESSED(R.string.export_option_compressed, R.string.export_option_compressed_description);

    fun getFileExtension(password: String? = null): String {
        val baseExtension =
            when (this) {
                PLAIN -> "json"
                COMPRESSED -> "json.gz"
            }
        return if (!password.isNullOrEmpty()) "$baseExtension.bin" else baseExtension
    }
}
