import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktfmt.gradle)
    alias(libs.plugins.refine)
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        extraWarnings = true
        freeCompilerArgs.add("-Xwarning-level=UNUSED_ANONYMOUS_PARAMETER:disabled")
        freeCompilerArgs.add("-Xwarning-level=REDUNDANT_VISIBILITY_MODIFIER:disabled")
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

android {
    namespace = "io.github.wifi_password_manager"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.wifi_password_manager"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
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
    arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
}

dependencies {
    // AndroidX
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))

    // Material Components
    implementation(libs.material.components)

    // FileKit
    implementation(libs.filekit.core)
    implementation(libs.filekit.dialogs)

    // KotlinX
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    // Shizuku
    compileOnly(project(":hidden-api"))
    implementation(libs.hiddenapibypass)
    implementation(libs.refine.runtime)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)

    // Koin
    implementation(libs.koin.androidx.startup)
    implementation(libs.koin.annotations)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.compose)
    implementation(libs.koin.core)
    ksp(libs.koin.ksp.compiler)

    // Testing
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.junit)

    // Debug
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)
}
