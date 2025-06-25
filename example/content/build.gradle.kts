import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.tiamat.destinations.compiler)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    jvm()
    androidLibrary {
        namespace = "composegears.tiamat.example.content"
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget = JvmTarget.JVM_1_8
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.tiamat)
            api(projects.tiamatDestinations.tiamatDestinations)
            api(projects.example.platform)
            api(projects.example.extra)
            api(projects.example.uiCore)

            implementation(compose.components.uiToolingPreview)
            implementation(libs.compose.material3.window.size)
        }
    }
}