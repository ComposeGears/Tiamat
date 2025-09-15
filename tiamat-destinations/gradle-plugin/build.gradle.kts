import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.m2p)
}

version = tiamat.versions.tiamat.get()
group = "io.github.composegears"

kotlin {
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        enabled = true
    }
}

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

buildConfig {
    packageName("com.composegears.tiamat.destinations")
    buildConfigField<String>("COMPILER_PLUGIN_VERSION", tiamat.versions.tiamat.get())

    useKotlinOutput()
}

tasks.register<Jar>("sourcesJar") {
    group = "build"
    description = "Assembles Kotlin sources"
    archiveClassifier = "sources"
    from(sourceSets.main.get().allSource)
    dependsOn(tasks.classes)
}

publishing {
    publications {
        create<MavenPublication>("gradlePlugin") {
            artifact(tasks["sourcesJar"])
        }
    }
}

m2p {
    description = "Tiamat Destinations Gradle Plugin"
}