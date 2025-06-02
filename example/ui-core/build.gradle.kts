import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    jvm()
    androidLibrary {
        namespace = "composegears.tiamat.example.ui.core"
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
            implementation(projects.tiamat)

            api(compose.foundation)
            api(compose.material3)
            api(compose.materialIconsExtended)
            api(compose.runtime)
            api(compose.ui)
        }
    }
}
