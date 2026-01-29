import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.tiamat.destinations.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    androidLibrary {
        namespace = "com.composegears.tiamat.sample"
        compileSdk = tiamat.versions.compileSdk.get().toInt()
        minSdk = tiamat.versions.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "TiamatApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.tiamat)
            api(projects.tiamatDestinations.tiamatDestinations)
            api(libs.compose.foundation)
            api(libs.compose.ui.tooling.preview)
            api(libs.compose.material3)
            api(libs.compose.material3.window.size)
            api(libs.koin.compose.viewmodel)
        }
    }
}