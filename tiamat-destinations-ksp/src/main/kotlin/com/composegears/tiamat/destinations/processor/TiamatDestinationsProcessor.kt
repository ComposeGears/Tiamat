package com.composegears.tiamat.destinations.processor

import com.composegears.tiamat.destinations.InstallIn
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

class TiamatDestinationsProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Find all items annotated with InstallIn
        val symbols = resolver.getSymbolsWithAnnotation(InstallIn::class.qualifiedName!!)
            .filter { it.validate() }
            .toList()

        if (symbols.isEmpty()) {
            return emptyList()
        }

        // Group destinations by key
        val genInfo = mutableMapOf<String, MutableList<NotationInfo>>()

        val typeOfNavDestination = resolver
            .getClassDeclarationByName("com.composegears.tiamat.NavDestination")
            ?.asStarProjectedType() ?: error("Unable to resolve NavDestination class")

        symbols.forEach { symbol ->
            // Get the annotation
            val annotation = symbol.annotations.first {
                it.shortName.asString() == "InstallIn" &&
                    it.annotationType.resolve().declaration.qualifiedName?.asString() == InstallIn::class.qualifiedName
            }

            // Extract annotation parameter (the destination type)
            val key = annotation.arguments.first().value as String

            fun error(message: String) = logger.error(message, symbol)

            val notation = when (symbol) {
                is KSPropertyDeclaration -> {
                    val isNavDestination = typeOfNavDestination.isAssignableFrom(symbol.type.resolve())
                    if (!isNavDestination) {
                        error("Property ${symbol.simpleName.asString()} annotated with @InstallIn must be of type NavDestination<*>")
                        null
                    } else symbol.qualifiedName?.asString()
                }
                is KSClassDeclaration -> {
                    val isObject = symbol.classKind == ClassKind.OBJECT
                    if (!isObject) {
                        error("Class ${symbol.simpleName.asString()} annotated with @InstallIn must be an object")
                        null
                    } else symbol.qualifiedName?.asString()
                }
                else -> {
                    error("Element annotated with @InstallIn must be an object or property")
                    null
                }
            }

            if (notation == null) return@forEach

            genInfo
                .getOrPut(key) { mutableListOf() }
                .add(NotationInfo(symbol.containingFile, notation))
        }

        // Generate the mapping class
        generateMappingClass(codeGenerator, genInfo)

        return symbols.filterNot { it.validate() }.toList()
    }

    private fun generateMappingClass(
        codeGenerator: CodeGenerator,
        genInfo: Map<String, List<NotationInfo>>,
    ) {
        val packageName = "com.composegears.tiamat.destinations.internal"
        val className = "TiamatDestinationMapping"

        val files = genInfo
            .entries
            .flatMap { it.value }
            .mapNotNull { it.file }
            .toTypedArray()

        codeGenerator.createNewFile(
            dependencies = Dependencies(true, *files),
            packageName = packageName,
            fileName = className,
            extensionName = "kt"
        )
            .bufferedWriter()
            .use {
                val mappings = genInfo.entries.joinToString(separator = ",\n") { (dest, items) ->
                    val itemsList = items.joinToString(separator = ",\n") { n -> "            ${n.notation}" }
                    "        \"$dest\" to arrayOf(\n$itemsList\n        )"
                }
                it.write(
                    """
                        |package $packageName
                        |
                        |object $className {
                        |
                        |    private val mapping: Map<String, Array<com.composegears.tiamat.NavDestination<*>>> = mapOf(
                        |$mappings   
                        |    ) 
                        |
                        |    infix fun of(key: String) = mapping[key] ?: error("No destinations found for key: ${'$'}key")
                        |}
                    """.trimMargin("|")
                )
            }
    }

    data class NotationInfo(
        val file: KSFile?,
        val notation: String
    )
}