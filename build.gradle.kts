plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
}

tasks.register("createM2Zip") {
    dependsOn(m2pPublishTasks())
    doLast {
        val m2Dir = layout.buildDirectory.dir("m2")
        fileTree(m2Dir).files.onEach {
            if (
                it.name.endsWith(".asc.md5") or
                it.name.endsWith(".asc.sha1") or
                it.name.endsWith(".sha256") or
                it.name.endsWith(".sha256") or
                it.name.endsWith(".sha512") or
                it.name.equals("maven-metadata.xml.sha1") or
                it.name.equals("maven-metadata.xml.md5") or
                it.name.equals("maven-metadata.xml")
            ) it.delete()
        }
    }
}