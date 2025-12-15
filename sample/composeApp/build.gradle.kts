import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.tiamat.destinations.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

kotlin {
    jvm()
    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
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

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "tiamatApp"
        browser {
            commonWebpackConfig {
                outputFileName = "tiamatApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static(project.projectDir.path)
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.tiamat)
            implementation(projects.tiamatDestinations.tiamatDestinations)

            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.window.size)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.compose)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.hilt.compose)
            implementation(libs.hilt.android)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.coroutines.swing)
        }
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling.preview)
    add("kspAndroid", libs.hilt.compiler)
}

android {
    namespace = "composegears.tiamat.sample"
    compileSdk = libs.versions.sample.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "composegears.tiamat.sample"
        minSdk = libs.versions.sample.minSdk.get().toInt()
        targetSdk = libs.versions.sample.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

compose.desktop {
    application {
        mainClass = "composegears.tiamat.sample.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "composegears.tiamat.sample"
            packageVersion = "1.0.0"
        }
    }
}