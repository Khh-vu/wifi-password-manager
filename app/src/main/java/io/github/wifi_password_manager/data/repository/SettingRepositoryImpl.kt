package io.github.wifi_password_manager.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import io.github.wifi_password_manager.domain.model.Settings
import io.github.wifi_password_manager.domain.repository.SettingRepository
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.invoke
import kotlinx.serialization.json.Json

class SettingRepositoryImpl(
    private val context: Context,
    private val json: Json,
    private val dispatcher: CoroutineDispatcher,
) : SettingRepository {
    private val Context.dataStore: DataStore<Settings> by
        dataStore(
            fileName = "setting.json",
            serializer =
                object : Serializer<Settings> {
                    override val defaultValue: Settings = Settings()

                    override suspend fun readFrom(input: InputStream): Settings = dispatcher {
                        try {
                            json.decodeFromString(input.readBytes().decodeToString())
                        } catch (_: Exception) {
                            defaultValue
                        }
                    }

                    override suspend fun writeTo(t: Settings, output: OutputStream) = dispatcher {
                        output.write(json.encodeToString(t).encodeToByteArray())
                    }
                },
        )

    override val settings: StateFlow<Settings> =
        context.dataStore.data.stateIn(
            scope = CoroutineScope(dispatcher + SupervisorJob()),
            started = SharingStarted.Eagerly,
            initialValue = Settings(),
        )

    override suspend fun updateSettings(transform: suspend (Settings) -> Settings) {
        context.dataStore.updateData(transform)
    }
}
