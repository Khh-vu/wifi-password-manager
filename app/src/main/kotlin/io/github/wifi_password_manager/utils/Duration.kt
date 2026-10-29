package io.github.wifi_password_manager.utils

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import io.github.wifi_password_manager.R
import kotlin.time.Duration
import kotlin.time.DurationUnit

fun Duration.toValueAndUnit(): Pair<Long, DurationUnit> =
    when {
        inWholeSeconds % 60 != 0L || this == Duration.ZERO -> inWholeSeconds to DurationUnit.SECONDS
        else -> inWholeMinutes to DurationUnit.MINUTES
    }

val Duration.label: String
    @Composable
    @ReadOnlyComposable
    get() {
        if (isInfinite()) return stringResource(R.string.lock_timeout_never)
        if (this == Duration.ZERO) return stringResource(R.string.lock_timeout_immediately)
        val locale = LocalConfiguration.current.locales[0] ?: LocalLocale.current.platformLocale
        val (value, unit) = toValueAndUnit()
        val measureUnit =
            if (unit == DurationUnit.MINUTES) {
                MeasureUnit.MINUTE
            } else {
                MeasureUnit.SECOND
            }

        return MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.WIDE)
            .format(Measure(value, measureUnit))
    }

val DurationUnit.title: String
    @Composable
    @ReadOnlyComposable
    get() =
        stringResource(
            if (this == DurationUnit.MINUTES) R.string.lock_timeout_unit_minutes
            else R.string.lock_timeout_unit_seconds
        )
