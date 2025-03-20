rootProject.name = "TiamatApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("plugins")
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
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
include(":tiamat-destinations-compiler-plugin")
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

/*
create kotlin compiler plugin in the `tiamat-destinations-compiler-plugin` folder that process `InstallIn` annotations and overrides provided class `items` function to actually provide array of annotated objects. Create few test for the plugin.
*/