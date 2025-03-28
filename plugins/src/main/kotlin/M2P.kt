import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension
import java.io.File

interface M2PExtension {
    val description: Property<String>
}

class M2P : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        val extension = extensions.create<M2PExtension>("m2p_ext")
        plugins.apply("maven-publish")
        plugins.apply("signing")
        configure<PublishingExtension> {
            publications.withType<MavenPublication> {
                // Stub javadoc.jar artifact
                artifact(tasks.register<Jar>("${name}JavadocJar") {
                    archiveClassifier.set("javadoc")
                    archiveAppendix.set(this@withType.name)
                })
                // Provide artifacts information required by Maven Central
                pom {
                    name.set("Tiamat")
                    description.set(extension.description)
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
                    var rootGradle = gradle
                    while (rootGradle.parent != null) rootGradle = rootGradle.parent!!
                    url = uri(File(rootGradle.rootProject.rootDir, "build/m2"))
                }
            }
        }
        project.configure<SigningExtension> {
            val pgpKey = project.properties["PGP_KEY"]?.toString()?.replace("|", "\n")
            val pgpPas = project.properties["PGP_PAS"]?.toString()
            if (!pgpPas.isNullOrBlank() && !pgpKey.isNullOrBlank()) {
                println("signing")
                useInMemoryPgpKeys(pgpKey, pgpPas)
                val publications = extensions.findByType<PublishingExtension>()?.publications
                sign(publications)
            } else println("no signing information provided")
        }
    }
}

fun Project.m2p(action: M2PExtension.() -> Unit) {
    configure<M2PExtension>(action)
}