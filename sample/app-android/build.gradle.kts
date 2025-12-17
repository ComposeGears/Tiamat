import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "composegears.tiamat.app"
    compileSdk = libs.versions.sample.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "composegears.tiamat.app"
        minSdk = libs.versions.sample.minSdk.get().toInt()
        targetSdk = libs.versions.sample.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin.compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
}


dependencies {
    implementation(projects.sample.shared)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.compose)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.hilt.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}