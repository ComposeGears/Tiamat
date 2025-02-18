import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
}

android {
    namespace = "composegears.tiamat.example.platform"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}

kotlin {
    jvm()
    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        nodejs()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.tiamat)
            implementation(projects.example.uiCore)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}
