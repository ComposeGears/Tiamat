plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.m2p)
}

version = "1.5.0"
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

afterEvaluate {
    tasks["signPluginMavenPublication"].dependsOn(tasks["publishGradlePluginPublicationToMavenRepository"])
}

m2p {
    artifactId = "tiamat-destinations-gradle-plugin"
    description = "Tiamat Destinations Gradle Plugin"
}

tasks.register("sourcesJar", Jar::class) {
    group = "build"
    description = "Assembles Kotlin sources"

    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
    dependsOn(tasks.classes)
}

publishing {
    publications {
        create<MavenPublication>("gradlePlugin") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
        }
    }
}