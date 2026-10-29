package io.github.wifi_password_manager.receivers

import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.contentValuesOf
import androidx.core.database.getLongOrNull
import io.github.wifi_password_manager.domain.model.ExportOption
import io.github.wifi_password_manager.domain.repository.FileRepository
import io.github.wifi_password_manager.domain.repository.SettingRepository
import io.github.wifi_password_manager.domain.repository.WifiRepository
import io.github.wifi_password_manager.utils.Crypto
import io.github.wifi_password_manager.utils.groupAndSortedBySsid
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ExportWifiReceiver : BroadcastReceiver(), KoinComponent {
    companion object {
        private const val TAG = "ExportWifiReceiver"

        const val ACTION_EXPORT_WIFI = "io.github.wifi_password_manager.EXPORT_WIFI"
        const val EXTRA_FORMAT = "format"
        const val EXTRA_PASSWORD = "password"
        const val EXTRA_FILE_NAME = "fileName"
    }

    private val settingRepository by inject<SettingRepository>()
    private val wifiRepository by inject<WifiRepository>()
    private val fileRepository by inject<FileRepository>()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_EXPORT_WIFI) return

        val appContext = context.applicationContext
        val mimeTypeMap = MimeTypeMap.getSingleton()

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO)
            .launch {
                if (!settingRepository.getCurrentSettings().allowInsecureReceiver) return@launch

                val exportOption = extractExportOption(intent) ?: ExportOption.PLAIN
                val password = intent.getStringExtra(EXTRA_PASSWORD)
                val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: defaultFileName

                val extension = exportOption.getFileExtension(password)
                val mimeType = mimeTypeMap.getMimeTypeFromExtension(extension) ?: "*/*"
                val uri = getOutputUri(appContext, mimeType, fileName, extension) ?: return@launch

                val bytes = getExportData(exportOption, password)
                appContext.contentResolver.openOutputStream(uri, "wt")?.use { it.write(bytes) }
            }
            .invokeOnCompletion {
                pendingResult.finish()
                if (it != null) {
                    Log.e(TAG, "Job Failed during export", it)
                } else {
                    Log.d(TAG, "Job export process completed.")
                }
            }
    }

    private val defaultFileName: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")
            return "WiFi_${LocalDateTime.now().format(formatter)}"
        }

    private fun extractExportOption(intent: Intent): ExportOption? {
        return try {
            val format = intent.getStringExtra(EXTRA_FORMAT) ?: return null
            ExportOption.valueOf(format.uppercase())
        } catch (_: Throwable) {
            null
        }
    }

    private fun getOutputUri(
        context: Context,
        mimeType: String,
        name: String,
        extension: String,
    ): Uri? {
        val displayName = "$name.$extension"
        val existingUri =
            context.contentResolver
                .query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.MediaColumns._ID),
                    "${MediaStore.MediaColumns.RELATIVE_PATH}=? AND ${MediaStore.MediaColumns.DISPLAY_NAME}=?",
                    arrayOf("${Environment.DIRECTORY_DOWNLOADS}/", displayName),
                    null,
                )
                .use { c ->
                    c?.takeIf { it.count == 1 }
                        ?.apply { moveToFirst() }
                        ?.getLongOrNull(c.getColumnIndex(MediaStore.MediaColumns._ID))
                        ?.let {
                            ContentUris.withAppendedId(
                                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                it,
                            )
                        }
                }

        return existingUri
            ?: context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValuesOf(
                    MediaStore.Downloads.DISPLAY_NAME to displayName,
                    MediaStore.Downloads.MIME_TYPE to mimeType,
                    MediaStore.Downloads.RELATIVE_PATH to Environment.DIRECTORY_DOWNLOADS,
                ),
            )
    }

    private suspend fun getExportData(option: ExportOption, password: String?): ByteArray {
        val networks = wifiRepository.getAllNetworksList().groupAndSortedBySsid()
        val data =
            when (option) {
                ExportOption.PLAIN -> fileRepository.networksToJson(networks).toByteArray()
                ExportOption.COMPRESSED -> fileRepository.networksToGZip(networks)
            }

        return if (!password.isNullOrEmpty()) {
            Crypto.encrypt(data, password, option)
        } else {
            data
        }
    }
}
