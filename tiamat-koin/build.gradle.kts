plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.m2p)
}

val libName = "io.github.composegears"
val libVersion = "1.0.2"

group = libName
version = libVersion

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.tiamat)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(compose.runtime)
        }
    }
}

android {
    namespace = libName
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

m2p {
    description = "Tiamat Koin integration"
}