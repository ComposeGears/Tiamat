package com.composegears.tiamat.destinations

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.getLogger
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class TiamatDestinationsComponentRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = "tiamat-destinations-compiler"

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        configuration.getLogger().log("[TD] Registering Tiamat Destinations component")
        IrGenerationExtension.registerExtension(
            TiamatDestinationsIrGenerationExtension(configuration.getLogger())
        )
    }
}