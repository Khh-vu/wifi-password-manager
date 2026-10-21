import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask

plugins {
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.koin.compiler) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktfmt.gradle)
    alias(libs.plugins.refine) apply false
    alias(libs.plugins.room3) apply false
    alias(libs.plugins.stability.analyzer) apply false
}

ktfmt { kotlinLangStyle() }

tasks.register<KtfmtFormatTask>("ktfmtPrecommit") {
    description = "Run ktfmt formatter before commit"
    source = project.fileTree(rootDir)
    include("**/*.kt", "**/*.kts")
    exclude("**/build/*")
}
