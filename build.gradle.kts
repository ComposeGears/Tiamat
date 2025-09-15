import io.gitlab.arturbosch.detekt.DetektPlugin

plugins {
    base // expose `clear` task, so we can modify it
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.hot.reload) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.m2p) apply false
}

detekt {
    config.from(files("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
}

// not include `includeBuild` (tiamat-dest gradle & kotlin plugins)
// as they are not projects  (they are count as included-projects)
allprojects {
    apply<DetektPlugin>()
    detekt {
        buildUponDefaultConfig = true
        parallel = true
        autoCorrect = true
        source.from(
            files(
                "src/commonMain/kotlin",
                "src/commonTest/kotlin",
                "src/jvmMain/kotlin",
                "src/desktopMain/kotlin",
                "src/androidMain/kotlin",
                "src/iosMain/kotlin",
                "src/wasmJsMain/kotlin",
            )
        )
    }

    dependencies {
        detektPlugins(rootProject.project.libs.detekt.compose)
        detektPlugins(rootProject.project.libs.detekt.formatting)
    }
}

// root `clean` task not include subprojects by default, so add them directly
rootProject.tasks["clean"].apply {
    dependsOn(gradle.includedBuild("tiamat-destinations-compiler").task(":clean"))
    dependsOn(gradle.includedBuild("tiamat-destinations-gradle-plugin").task(":clean"))
}

rootProject.tasks.register("createLocalM2") {
    val publishTasks = allprojects
        .filter { it.extensions.findByType<M2PExtension>() != null }
        .map { it.tasks["publish"] }
    dependsOn(publishTasks)
    dependsOn(gradle.includedBuild("tiamat-destinations-compiler").task(":publish"))
    dependsOn(gradle.includedBuild("tiamat-destinations-gradle-plugin").task(":publish"))
    doLast {
        val m2Dir = File(rootDir, "build/m2")
        fileTree(m2Dir).files.onEach {
            if (
                it.name.endsWith(".asc.md5") or
                it.name.endsWith(".asc.sha1") or
                it.name.endsWith(".sha256") or
                it.name.endsWith(".sha512") or
                it.name.equals("maven-metadata.xml.sha1") or
                it.name.equals("maven-metadata.xml.md5") or
                it.name.equals("maven-metadata.xml")
            ) it.delete()
        }
    }
}

rootProject.tasks.register("tiamatCleanJvmTest") {
    dependsOn(":tiamat:cleanJvmTest")
    dependsOn("tiamat-destinations:tiamat-destinations:cleanJvmTest")
}

rootProject.tasks.register("tiamatJvmTests") {
    dependsOn(":tiamat:jvmTest")
    dependsOn("tiamat-destinations:tiamat-destinations:jvmTest")
    dependsOn(gradle.includedBuild("tiamat-destinations-compiler").task(":test"))
}

rootProject.tasks["tiamatJvmTests"].shouldRunAfter("tiamat-destinations:tiamat-destinations:cleanJvmTest")