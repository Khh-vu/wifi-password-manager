import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.aboutLibraries)
    alias(libs.plugins.aboutLibraries.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktfmt.gradle)
    alias(libs.plugins.refine)
    alias(libs.plugins.room)
    alias(libs.plugins.stability.analyzer)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        progressiveMode = true
        languageVersion = KotlinVersion.KOTLIN_2_3
        apiVersion = KotlinVersion.KOTLIN_2_3
    }
}

android {
    namespace = "io.github.wifi_password_manager"
    compileSdk { version = release(36) }

    defaultConfig {
        applicationId = "io.github.wifi_password_manager"
        minSdk = 30
        targetSdk = 36
        versionCode = 8
        versionName = "1.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val keystoreProperties =
            Properties().apply {
                val keystorePropertiesFile = rootProject.file("key.properties")
                if (!keystorePropertiesFile.exists()) return@signingConfigs
                keystorePropertiesFile.inputStream().use { load(it) }
            }
        register("release") {
            storeFile = keystoreProperties["storeFile"]?.let { file(it) }
            storePassword = keystoreProperties["storePassword"].toString()
            keyAlias = keystoreProperties["keyAlias"].toString()
            keyPassword = keystoreProperties["keyPassword"].toString()
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}

ktfmt { kotlinLangStyle() }

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
    arg("KOIN_DEFAULT_MODULE", "false")
    arg("KOIN_LOG_TIMES", "true")
}

dependencies {
    // AndroidX
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.biometric.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    implementation(platform(libs.androidx.compose.bom))
    ksp(libs.room.compiler)

    // Material Components
    implementation(libs.material.components)

    // FileKit
    implementation(libs.filekit.core)
    implementation(libs.filekit.dialogs)

    // JetBrains
    implementation(libs.adaptive)
    implementation(libs.adaptive.navigation3)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.viewmodel.navigation3)

    // Privileged
    compileOnly(libs.stub)
    compileOnly(projects.hiddenApi)
    implementation(libs.hiddenapibypass)
    implementation(libs.refine.runtime)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)

    // Koin
    implementation(libs.koin.androidx.workmanager)
    implementation(libs.koin.annotations)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.core)
    ksp(libs.koin.ksp.compiler)

    // QR Code
    implementation(libs.qrose)

    // AboutLibraries
    implementation(libs.aboutlibraries.compose.core)
    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.aboutlibraries.core)

    // Testing
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.junit)

    // Debug
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.leakcanary.android)
}

room {
    generateKotlin = true
    schemaDirectory("$projectDir/schemas")
}
