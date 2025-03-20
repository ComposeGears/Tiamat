import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "composegears.tiamat.example.content"
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
            api(projects.tiamat)
            api(projects.tiamatDestinations)
            api(projects.example.platform)
            api(projects.example.extra)
            api(projects.example.uiCore)

            implementation(compose.components.uiToolingPreview)
        }
    }
}

dependencies{
    add("kspCommonMainMetadata", projects.tiamatDestinationsKsp)
}