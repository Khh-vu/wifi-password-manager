package io.github.wifi_password_manager.utils

import androidx.window.core.layout.WindowSizeClass

private enum class SizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED;

    val isCompact: Boolean
        get() = this == COMPACT

    val isMedium: Boolean
        get() = this == MEDIUM

    val isExpanded: Boolean
        get() = this == EXPANDED
}

private val WindowSizeClass.heightClass: SizeClass
    get() =
        when {
            isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND) -> {
                SizeClass.EXPANDED
            }

            isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) -> {
                SizeClass.MEDIUM
            }

            else -> {
                SizeClass.COMPACT
            }
        }

private val WindowSizeClass.widthClass: SizeClass
    get() =
        when {
            isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> {
                SizeClass.EXPANDED
            }

            isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> {
                SizeClass.MEDIUM
            }

            else -> {
                SizeClass.COMPACT
            }
        }

enum class DeviceConfiguration {
    MOBILE_PORTRAIT,
    MOBILE_LANDSCAPE,
    TABLET_PORTRAIT,
    TABLET_LANDSCAPE,
    DESKTOP;

    companion object {
        fun fromWindowSizeClass(windowSizeClass: WindowSizeClass): DeviceConfiguration {
            val widthClass = windowSizeClass.widthClass
            val heightClass = windowSizeClass.heightClass

            return when {
                widthClass.isCompact && heightClass.isMedium -> MOBILE_PORTRAIT
                widthClass.isCompact && heightClass.isExpanded -> MOBILE_PORTRAIT
                widthClass.isExpanded && heightClass.isCompact -> MOBILE_LANDSCAPE
                widthClass.isMedium && heightClass.isExpanded -> TABLET_PORTRAIT
                widthClass.isExpanded && heightClass.isMedium -> TABLET_LANDSCAPE
                else -> DESKTOP
            }
        }
    }
}
