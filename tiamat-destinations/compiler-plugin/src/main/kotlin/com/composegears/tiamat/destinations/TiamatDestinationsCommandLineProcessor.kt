package com.composegears.tiamat.destinations

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class TiamatDestinationsCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "TiamatDestinationsCompiler"

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            optionName = TiamatDestinationsConfigurationKeys.DUMP_DIR_OPTION,
            valueDescription = "Path to the directory where the plugin should dump its output",
            description = "Directory path for Tiamat Destinations plugin output",
            required = false
        )
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            TiamatDestinationsConfigurationKeys.DUMP_DIR_OPTION -> configuration.put(
                TiamatDestinationsConfigurationKeys.DUMP_DIR,
                value
            )
        }
    }
}