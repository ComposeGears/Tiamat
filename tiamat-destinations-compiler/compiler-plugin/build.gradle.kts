plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.m2p)
}

version = "1.0.0"
group = "io.github.composegears"

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)

    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(libs.zacsweers.kctfork)
    testImplementation(libs.junit)
}

m2p {
    artifactId = "tiamat-destinations-compiler"
    description = "Tiamat Destinations Compiler Plugin"
}

publishing {
    publications {
        create<MavenPublication>("kotlinCompilerPlugin") {
            from(components["kotlin"])
        }
    }
}