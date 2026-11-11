import io.github.wifi_password_manager.buildlogic.FetchAospSourcesTask

tasks.register<FetchAospSourcesTask>("fetchAospSources") {
    description = "Fetch AOSP Wi-Fi sources"
    group = "help"
    outputDir = layout.projectDirectory.dir("aosp-sources")
}
