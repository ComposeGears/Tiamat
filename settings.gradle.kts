rootProject.name = "TiamatApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("plugins")
    includeBuild("tiamat-destinations-compiler")
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

include(":tiamat")
include(":tiamat-destinations")
include(":tiamat-koin")

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