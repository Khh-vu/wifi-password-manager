package io.github.wifi_password_manager.domain.model

import androidx.annotation.StringRes
import io.github.wifi_password_manager.R

enum class ExportOption {
    PLAIN,
    COMPRESSED;

    val titleResId: Int
        @StringRes
        get() =
            when (this) {
                PLAIN -> R.string.export_option_plain
                COMPRESSED -> R.string.export_option_compressed
            }

    val descriptionResId: Int
        @StringRes
        get() =
            when (this) {
                PLAIN -> R.string.export_option_plain_description
                COMPRESSED -> R.string.export_option_compressed_description
            }
}
