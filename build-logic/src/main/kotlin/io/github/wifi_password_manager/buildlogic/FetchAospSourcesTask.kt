package io.github.wifi_password_manager.buildlogic

import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Base64
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Download from the network")
abstract class FetchAospSourcesTask : DefaultTask() {
    @get:Internal abstract val outputDir: DirectoryProperty

    @TaskAction
    fun fetch() {
        val client = HttpClient.newHttpClient()
        for ((api, branch) in BRANCHES) {
            val (frameworkUrl, serviceUrl) = baseUrls(api, branch)
            val dir = outputDir.get().asFile.resolve("api-$api")
            FRAMEWORK_FILES.forEach {
                download(client, "$frameworkUrl/$it", dir.resolve("framework/$it"))
            }
            SERVICE_FILES.forEach {
                download(client, "$serviceUrl/$it", dir.resolve("service/$it"))
            }
            dir.resolve("BRANCH").writeText("$branch\n")
        }
    }

    private fun baseUrls(api: Int, branch: String): Pair<String, String> {
        val ref = "+/refs/heads/$branch"
        return if (api <= 30) {
            "$ROOT/frameworks/base/$ref/wifi/java/android/net/wifi" to
                "$ROOT/frameworks/opt/net/wifi/$ref/service/java/com/android/server/wifi"
        } else {
            "$ROOT/packages/modules/Wifi/$ref/framework/java/android/net/wifi" to
                "$ROOT/packages/modules/Wifi/$ref/service/java/com/android/server/wifi"
        }
    }

    private fun download(client: HttpClient, url: String, dest: File) {
        if (dest.length() > 0) return
        val request = HttpRequest.newBuilder(URI("$url?format=TEXT")).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 200) {
            dest.parentFile.mkdirs()
            dest.writeBytes(Base64.getMimeDecoder().decode(response.body()))
            logger.lifecycle("OK   $url")
        } else {
            logger.lifecycle("MISS $url (HTTP ${response.statusCode()})")
        }
        Thread.sleep(REQUEST_DELAY_MS)
    }

    private companion object {
        const val ROOT = "https://android.googlesource.com/platform"
        const val REQUEST_DELAY_MS = 2000L

        val BRANCHES =
            listOf(
                29 to "android10-release",
                30 to "android11-release",
                31 to "android12-release",
                32 to "android12L-release",
                33 to "android13-release",
                34 to "android14-release",
                35 to "android15-release",
                36 to "android16-release",
                37 to "android17-release",
            )

        val FRAMEWORK_FILES =
            listOf(
                "IWifiManager.aidl",
                "IActionListener.aidl",
                "WifiConfiguration.java",
                "WifiManager.java",
                "WifiInfo.java",
                "WifiEnterpriseConfig.java",
                "WifiSsid.java",
                "SecurityParams.java",
            )

        val SERVICE_FILES =
            listOf(
                "ClientModeImpl.java",
                "WifiServiceImpl.java",
                "WifiConfigManager.java",
                "WifiConfigurationUtil.java",
                "WifiConfigStore.java",
                "NetworkListStoreData.java",
                "WifiShellCommand.java",
                "WifiNetworkSuggestionsManager.java",
                "util/WifiPermissionsUtil.java",
                "util/WifiPermissionsWrapper.java",
                "util/XmlUtil.java",
            )
    }
}
