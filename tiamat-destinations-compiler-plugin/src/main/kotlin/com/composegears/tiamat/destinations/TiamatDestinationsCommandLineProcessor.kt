package com.composegears.tiamat.destinations

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class TiamatDestinationsCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "tiamat-destinations-compiler-plugin"

    override val pluginOptions: Collection<AbstractCliOption> = emptyList()

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        // No options to process
    }
}