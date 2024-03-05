plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    id("maven-publish")
    signing
}

val libName = "io.github.composegears"
val libVersion = "1.0.2"

group = libName
version = libVersion

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.lifecycle.runtime)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = libName
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

publishing {
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        // Provide artifacts information required by Maven Central
        pom {
            name.set("Tiamat")
            description.set("KMM Navigation library")
            url.set("https://github.com/ComposeGears/Tiamat")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("vkatz")
                    name.set("Viachaslau Katsuba")
                    url.set("https://github.com/vkatz")
                }
                developer {
                    id.set("egorikftp")
                    name.set("Yahor Urbanovich")
                    url.set("https://github.com/egorikftp")
                }
            }
            scm {
                url.set("https://github.com/ComposeGears/Tiamat")
            }
        }
    }
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("m2"))
        }
    }
}

signing {
    val pgpKey = project.properties["PGP_KEY"]?.toString()?.replace("|", "\n")
    val pgpPas = project.properties["PGP_PAS"]?.toString()
    if (!pgpPas.isNullOrBlank() && !pgpKey.isNullOrBlank()) {
        println("signing")
        useInMemoryPgpKeys(pgpKey, pgpPas)
        sign(publishing.publications)
    } else println("no signing information provided")
}

tasks.publish.get().doLast {
    fileTree(layout.buildDirectory.dir("m2")).files.onEach {
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