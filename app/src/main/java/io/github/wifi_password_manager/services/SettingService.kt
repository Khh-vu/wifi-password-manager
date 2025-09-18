package io.github.wifi_password_manager.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import io.github.wifi_password_manager.data.Settings
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.invoke
import kotlinx.serialization.json.Json

class SettingService(private val context: Context, private val json: Json) {
    private val Context.dataStore: DataStore<Settings> by
        dataStore(
            fileName = "setting.json",
            serializer =
                object : Serializer<Settings> {
                    override val defaultValue: Settings = Settings()

                    override suspend fun readFrom(input: InputStream): Settings =
                        Dispatchers.IO {
                            try {
                                json.decodeFromString(input.readBytes().decodeToString())
                            } catch (_: Exception) {
                                defaultValue
                            }
                        }

                    override suspend fun writeTo(t: Settings, output: OutputStream) =
                        Dispatchers.IO { output.write(json.encodeToString(t).encodeToByteArray()) }
                },
        )

    val settings: Flow<Settings> = context.dataStore.data

    suspend fun updateSettings(transform: suspend (Settings) -> Settings) {
        context.dataStore.updateData(transform)
    }
}
