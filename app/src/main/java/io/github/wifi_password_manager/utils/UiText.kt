package io.github.wifi_password_manager.utils

import android.content.res.Resources
import androidx.annotation.StringRes

sealed interface UiText {

    data class DynamicString(val value: String) : UiText

    class StringResource(@param:StringRes val resId: Int, vararg val args: Any) : UiText

    fun asString(resources: Resources): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> resources.getString(resId, *args)
        }
    }
}
