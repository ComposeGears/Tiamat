package com.composegears.tiamat.destinations

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class TiamatDestinationsCompilerPlugin : KotlinCompilerPluginSupportPlugin {

    override fun getCompilerPluginId(): String = "TiamatDestinationsCompiler"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "io.github.composegears",
        artifactId = "tiamat-destinations-compiler",
        version = BuildConfig.COMPILER_PLUGIN_VERSION
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        return project.provider { emptyList() }
    }
}