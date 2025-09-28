import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.m2p)
}

version = tiamat.versions.tiamat.get()
group = "io.github.composegears"

kotlin {
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        enabled = true
    }

    compilerOptions {
        freeCompilerArgs.addAll("-opt-in=androidx.compose.ui.ExperimentalComposeUiApi")
    }

    jvm()
    androidLibrary {
        namespace = "com.composegears.tiamat"
        compileSdk = tiamat.versions.compileSdk.get().toInt()
        minSdk = tiamat.versions.minSdk.get().toInt()

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
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)

            api(libs.compose.ui.backhandler)
            api(libs.lifecycle.runtime.compose)
            api(libs.lifecycle.viewmodel.compose)
        }
        androidMain.dependencies {
            api(libs.androidx.activity.compose)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        commonTest.dependencies {
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(compose.material3)
            implementation(libs.kotlin.test)
        }
    }
}

m2p {
    description = "Compose Multiplatform navigation library"
}

kover {
    reports {
        filters {
            excludes {
                this.annotatedBy("com.composegears.tiamat.ExcludeFromTests")
            }
        }
    }
}
