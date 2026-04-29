package io.github.wifi_password_manager.di

import android.content.Context
import io.github.wifi_password_manager.manager.PrivilegedManager
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class AppModule {
    @Single
    fun json(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        prettyPrint = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Single fun privilegedManager(context: Context) = PrivilegedManager(context)
}
