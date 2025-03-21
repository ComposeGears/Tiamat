plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    id("java-gradle-plugin")
    id("maven-publish")
}

group = "com.composegears.tiamat"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)
    compileOnly(libs.kotlin.gradle.plugin.api)

    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(libs.zacsweers.kctfork)
    testImplementation(libs.junit)
}

gradlePlugin {
    plugins {
        create("tiamatDestinationsPlugin") {
            id = "com.composegears.tiamat.destinations.compiler"
            implementationClass = "com.composegears.tiamat.destinations.TiamatDestinationsCompilerPlugin"
        }
    }
}