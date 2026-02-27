plugins { alias(libs.plugins.android.library) }

android {
    namespace = "io.github.wifi_password_manager.hidden_api"
    compileSdk { version = release(36) }

    defaultConfig {
        minSdk { version = release(30) }
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    annotationProcessor(libs.refine.annotation.processor)
    compileOnly(libs.refine.annotation)
    compileOnly(libs.stub)
    implementation(libs.androidx.annotation)
}
