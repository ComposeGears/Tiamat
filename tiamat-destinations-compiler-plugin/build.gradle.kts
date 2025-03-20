import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("maven-publish")
}

group = "com.composegears.tiamat"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.0.0")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.1.10")

    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.0.0")
    testImplementation("dev.zacsweers.kctfork:core:0.7.0")
    testImplementation("junit:junit:4.13.2")
}

gradlePlugin {
    plugins {
        create("tiamatDestinationsPlugin") {
            id = "com.composegears.tiamat.destinations"
            implementationClass = "com.composegears.tiamat.destinations.TiamatDestinationsCompilerPlugin"
        }
    }
}

//tasks.withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = "1.8"
//}