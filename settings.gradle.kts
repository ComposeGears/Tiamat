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
        create("libConfig") {
            from(files("libConfig.toml"))
        }
    }
}

include(":tiamat")
include(":tiamat-destinations")
include(":tiamat-koin")

includeBuild("tiamat-destinations-compiler/compiler-plugin") {
    name = "tiamat-destinations-compiler"
    dependencySubstitution {
        substitute(module("io.github.composegears:tiamat-destinations-compiler"))
            .using(project(":"))
    }
}
includeBuild("tiamat-destinations-compiler/gradle-plugin") {
    name = "tiamat-destinations-gradle-plugin"
}

include(":example:app:composeApp")
include(":example:content")
include(":example:extra")
include(":example:platform")
include(":example:ui-core")


project(":example:app:composeApp").name = "composeApp"
project(":example:content").name = "content"
project(":example:platform").name = "platform"
project(":example:extra").name = "extra"
project(":example:ui-core").name = "ui-core"