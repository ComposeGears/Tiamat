package com.composegears.tiamat.destinations

import org.jetbrains.kotlin.config.CompilerConfigurationKey

object TiamatDestinationsConfigurationKeys {
    const val DUMP_DIR_OPTION = "dumpDir"
    val DUMP_DIR: CompilerConfigurationKey<String> =
        CompilerConfigurationKey.create(DUMP_DIR_OPTION)
}

