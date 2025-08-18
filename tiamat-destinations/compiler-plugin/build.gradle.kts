plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.m2p)
}

version = tiamat.versions.tiamat.destinations.get()
group = "io.github.composegears"

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)

    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(libs.zacsweers.kctfork)
    testImplementation(libs.kotlin.test)
}

tasks.register("sourcesJar", Jar::class) {
    group = "build"
    description = "Assembles Kotlin sources"

    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
    dependsOn(tasks.classes)
}

publishing {
    publications {
        create<MavenPublication>("kotlinCompilerPlugin") {
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
        }
    }
}

m2p {
    description = "Tiamat Destinations Compiler Plugin"
}