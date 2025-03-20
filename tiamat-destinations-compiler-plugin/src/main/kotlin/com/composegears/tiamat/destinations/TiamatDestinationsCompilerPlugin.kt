package com.composegears.tiamat.destinations

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class TiamatDestinationsCompilerPlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
//        target.extensions.create("tiamatDestinations", TiamatDestinationsPluginExtension::class.java)
    }

    override fun getCompilerPluginId(): String = "tiamat-destinations-compiler-plugin"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.composegears.tiamat",
        artifactId = "tiamat-destinations-compiler-plugin",
        version = "1.0.0"
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        return project.provider { emptyList() }
    }
}

//open class TiamatDestinationsPluginExtension