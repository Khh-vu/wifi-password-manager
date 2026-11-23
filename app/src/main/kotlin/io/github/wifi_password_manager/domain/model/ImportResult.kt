package io.github.wifi_password_manager.domain.model

import androidx.annotation.StringRes
import io.github.wifi_password_manager.R

enum class ImportResult(@StringRes val messageResId: Int) {
    NOTHING_TO_IMPORT(R.string.no_network_to_import),
    ALREADY_SAVED(R.string.import_already_saved),
    SAVED_TO_APP(R.string.import_to_app_success),
    SAVED_TO_DEVICE(R.string.import_to_device_success),
    DEVICE_REJECTED_SOME(R.string.import_device_rejected_some),
}
