import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.DetektPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.gradle.dependency.check)
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.m2p) apply false
}

detekt {
    config.from(files("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf { !isStable(candidate.version) }
}

fun isStable(version: String) = "^[0-9,.v-]+(-r)?$".toRegex().matches(version)

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

subprojects {
    tasks.withType<KotlinCompile>().configureEach {
        val outPath = layout.buildDirectory.dir("compose_compiler").get().asFile.absoluteFile

        compilerOptions {
            if (project.findProperty("composeCompilerReports") == "true") {
                composeCompiler {
                    reportsDestination = outPath
                    metricsDestination = outPath
                }
            }
        }
    }
}

createM2PTask()