plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.m2p)
}

version = "1.5.0"
group = "io.github.composegears"

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)

    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(libs.zacsweers.kctfork)
    testImplementation(libs.kotlin.test)
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