import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

sourceSets.main {
    java.srcDir("kotlin")
}

dependencies {
    implementation(projects.sample.shared)
    implementation(libs.kotlin.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "composegears.tiamat.sample"
            packageVersion = "1.0.0"
        }
    }
}