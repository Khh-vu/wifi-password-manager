package io.github.wifi_password_manager.utils

import android.content.Context
import androidx.annotation.StringRes

sealed interface UiText {

    data class DynamicString(val value: String) : UiText

    class StringResource(@param:StringRes val resId: Int, vararg val args: Any) : UiText

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}
