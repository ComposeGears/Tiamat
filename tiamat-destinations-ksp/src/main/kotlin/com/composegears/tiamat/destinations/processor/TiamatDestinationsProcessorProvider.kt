package com.composegears.tiamat.destinations.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class TiamatDestinationsProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return TiamatDestinationsProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}