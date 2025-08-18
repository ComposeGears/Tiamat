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

include(":example:app:composeApp")
include(":example:content")
include(":example:platform")
include(":example:ui-core")


project(":example:app:composeApp").name = "composeApp"
project(":example:content").name = "content"
project(":example:platform").name = "platform"
project(":example:ui-core").name = "ui-core"