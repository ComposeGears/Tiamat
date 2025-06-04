import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.m2p)
}

version = libConfig.versions.tiamat.koin.get()
group = "io.github.composegears"

kotlin {
    explicitApi()

    jvm()
    androidLibrary {
        namespace = "com.composegears.tiamat.koin"
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

            implementation(libs.koin.compose)

            implementation(compose.foundation)
            implementation(compose.runtime)
        }
    }
}

m2p {
    description = "Tiamat Koin integration"
}