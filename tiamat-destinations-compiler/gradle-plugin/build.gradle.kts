plugins {
    kotlin("jvm") version libs.versions.kotlin
    id("java-gradle-plugin")
    alias(libs.plugins.m2p)
}

version = "1.0.0"
group = "io.github.composegears"

dependencies {
    compileOnly(libs.kotlin.gradle.plugin.api)
}

gradlePlugin {
    plugins {
        create("TiamatDestinationsGradlePlugin") {
            id = "io.github.composegears.tiamat.destinations.compiler"
            implementationClass = "com.composegears.tiamat.destinations.TiamatDestinationsCompilerPlugin"
        }
    }
}

m2p {
    artifactId = "tiamat-destinations-gradle-plugin"
    description = "Tiamat Destinations Gradle Plugin"
}

publishing {
    publications {
        create<MavenPublication>("gradlePlugin") {
            from(components["java"])
        }
    }
}