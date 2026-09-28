rootProject.name = "TiamatApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("plugins")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("tiamat") {
            from(files("gradle/tiamat.toml"))
        }
    }
}

include(":tiamat")
include(":tiamat-destinations:tiamat-destinations")

includeBuild("tiamat-destinations/compiler-plugin") {
    name = "tiamat-destinations-compiler"
    dependencySubstitution {
        substitute(module("io.github.composegears:tiamat-destinations-compiler")).using(project(":"))
    }
}
includeBuild("tiamat-destinations/gradle-plugin") {
    name = "tiamat-destinations-gradle-plugin"
}

include(":sample:shared")
include(":sample:app-android")
include(":sample:app-jvm")
include(":sample:app-wasm")