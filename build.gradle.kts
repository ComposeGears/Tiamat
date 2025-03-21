import io.gitlab.arturbosch.detekt.DetektPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.binary.compatibility)
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.m2p) apply false
}

apiValidation {
    val apiValidationProjects = listOf("tiamat", "tiamat-koin", "tiamat-destinations")
    ignoredProjects += allprojects.map { it.name } - apiValidationProjects
}

detekt {
    config.from(files("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
}

allprojects {
    group = "io.github.composegears"

    apply<DetektPlugin>()
    detekt {
        buildUponDefaultConfig = true
        parallel = true
        autoCorrect = true
        source.from(
            files(
                "src/commonMain/kotlin",
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