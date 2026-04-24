@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package com.composegears.tiamat.destinations

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.isObject
import org.jetbrains.kotlin.ir.util.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.Logger
import java.io.File

class TiamatDestinationsIrGenerationExtension(
    val logger: Logger,
    private val dumpDir: String? = null,
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

        logger.log("[TD] TiamatDestinationsIrGenerationExtension started")

        val installInAnnotation = FqName("com.composegears.tiamat.destinations.InstallIn")
        val tiamatGraphClass = ClassId(
            packageFqName = FqName("com.composegears.tiamat.destinations"),
            topLevelName = Name.identifier("TiamatGraph")
        )

        // Find all variables and classes annotated with @InstallIn
        val annotatedElements = mutableMapOf<IrClassSymbol, MutableList<IrSymbol>>()
        var hasErrors = false

        // Process all declarations in the module
        moduleFragment.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitProperty(declaration: IrProperty): IrStatement {
                val graphSymbols = declaration.annotations
                    .filter { it.type.classFqName == installInAnnotation }
                    .mapNotNull { it.arguments.firstOrNull()?.extractContainerClass()?.symbol }

                // Check for duplicate @InstallIn with the same graph
                val seen = mutableSetOf<IrClassSymbol>()
                for (symbol in graphSymbols) {
                    if (!seen.add(symbol)) {
                        logger.warning(
                            "[TD] Duplicate @InstallIn(${symbol.owner.name}::class) " +
                                "on property '${declaration.name}'. Each graph should appear at most once."
                        )
                    } else {
                        annotatedElements.getOrPut(symbol) { mutableListOf() }.add(declaration.symbol)
                    }
                }
                return super.visitProperty(declaration)
            }

            override fun visitClass(declaration: IrClass): IrStatement {
                val graphSymbols = declaration.annotations
                    .filter { it.type.classFqName == installInAnnotation }
                    .mapNotNull { it.arguments.firstOrNull()?.extractContainerClass()?.symbol }

                if (graphSymbols.isNotEmpty() && !declaration.isObject) {
                    // ERROR: @InstallIn on a non-object class is not allowed
                    logger.error(
                        "[TD] @InstallIn is not allowed on non-object class '${declaration.name}'. " +
                            "Only objects and properties of NavDestination type are supported. " +
                            "Use @InstallIn on a top-level property instead: " +
                            "@InstallIn(Graph::class) val screen = ${declaration.name}()"
                    )
                    hasErrors = true
                } else {
                    // Check for duplicate @InstallIn with the same graph
                    val seen = mutableSetOf<IrClassSymbol>()
                    for (symbol in graphSymbols) {
                        if (!seen.add(symbol)) {
                            logger.warning(
                                "[TD] Duplicate @InstallIn(${symbol.owner.name}::class) " +
                                    "on class '${declaration.name}'. Each graph should appear at most once."
                            )
                        } else {
                            annotatedElements.getOrPut(symbol) { mutableListOf() }.add(declaration.symbol)
                        }
                    }
                }
                return super.visitClass(declaration)
            }

            private fun IrExpression.extractContainerClass(): IrClass? {
                return when (val expression = this) {
                    is IrClassReference -> {
                        // Handle KClass reference: SomeClass::class
                        val classSymbol = expression.classType.classOrNull
                        classSymbol?.owner
                    }
                    is IrGetValue -> {
                        // Handle variable reference
                        val valueSymbol = expression.symbol
                        val valueType = valueSymbol.owner.type
                        valueType.classOrNull?.owner
                    }
                    else -> null
                }
            }
        })

        // Validate annotation values (must be TiamatGraph subclasses)
        val invalidGraphTargets = annotatedElements
            .map { it.key }
            .filter { !it.owner.isSubclassOf(tiamatGraphClass, pluginContext) }

        if (invalidGraphTargets.isNotEmpty()) {
            for (target in invalidGraphTargets) {
                logger.error(
                    "[TD] @InstallIn annotation value '${target.owner.name}' is not a TiamatGraph subclass. " +
                        "Annotation parameter should be a TiamatGraph subclass (do not use TiamatGraph itself)."
                )
            }
            hasErrors = true
        }

        // Validate annotation targets (must be NavDestination properties or objects)
        val invalidAnnotationTargets = annotatedElements
            .flatMap { it.value }
            .filter { !isValidAnnotationTarget(it, pluginContext) }

        if (invalidAnnotationTargets.isNotEmpty()) {
            for (target in invalidAnnotationTargets) {
                logger.error(
                    "[TD] @InstallIn target '$target' is not a valid NavDestination. " +
                        "Only objects and properties of NavDestination type are supported."
                )
            }
            hasErrors = true
        }

        // Stop if there were errors — do not attempt code generation
        if (hasErrors) return

        // Log info
        logger.log("[TD] Destinations map:")
        annotatedElements.forEach { (targetClass, elements) ->
            logger.log("[TD] ${targetClass.owner.name}: [")
            elements.forEach { element ->
                logger.log("[TD]   $element")
            }
            logger.log("[TD] ]")
        }

        // Dump graph to markdown if dumpDir is configured
        if (!dumpDir.isNullOrBlank()) {
            dumpGraphs(annotatedElements)
        }

        // For each TiamatGraph class, override the destinations() function
        moduleFragment.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitClass(declaration: IrClass): IrStatement {
                if (declaration.isSubclassOf(tiamatGraphClass, pluginContext)) {
                    logger.log("[TD] Found ${declaration.name}")
                    val annotatedForThisClass = annotatedElements[declaration.symbol]
                    if (!annotatedForThisClass.isNullOrEmpty()) {
                        // Create or override the items() function
                        logger.log("[TD] Modifying ${declaration.name}::destinations() function")
                        addOrReplaceItemsFunction(declaration, annotatedForThisClass, pluginContext)
                    }
                }

                return super.visitClass(declaration)
            }
        })
    }

    private fun dumpGraphs(annotatedElements: Map<IrClassSymbol, List<IrSymbol>>) {
        val dir = File(dumpDir!!)
        dir.mkdirs()
        val file = File(dir, "report.md")

        val sb = StringBuilder()
        for ((graphSymbol, elements) in annotatedElements) {
            val graphName = graphSymbol.owner.name.asString()
            sb.appendLine("## $graphName")
            for (symbol in elements) {
                val name = when (symbol) {
                    is IrPropertySymbol -> symbol.owner.name.asString()
                    is IrClassSymbol -> symbol.owner.name.asString()
                    else -> symbol.toString()
                }
                sb.appendLine("- $name")
            }
            sb.appendLine()
        }

        file.writeText(sb.toString().trimEnd() + "\n")
        logger.log("[TD] Dumped graph report to ${file.absolutePath}")
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun addOrReplaceItemsFunction(
        irClass: IrClass,
        elements: List<IrSymbol>,
        pluginContext: IrPluginContext
    ) {
        val navDestinationType = pluginContext.referenceClass(
            ClassId(
                FqName("com.composegears.tiamat.navigation"),
                Name.identifier("NavDestination")
            )
        )!!

        // Remove defined function (cover from those who may try to override by their own)
        irClass
            .declarations
            .filterIsInstance<IrSimpleFunction>()
            .find { it.name.asString() == "destinations" }
            ?.let { irClass.declarations.remove(it) }

        // Create mew `items` function
        irClass.addFunction(
            name = "destinations",
            modality = Modality.OPEN,
            returnType = pluginContext.irBuiltIns.arrayClass.typeWith(navDestinationType.defaultType)
        ).apply {
            // Build the function body that returns array of NavDestination objects
            val irBuilder = DeclarationIrBuilder(pluginContext, this.symbol)

            body = irBuilder.irBlockBody {
                // Add each annotated element to the array
                val args: List<IrExpression> = elements.mapNotNull { symbol ->
                    when (symbol) {
                        is IrPropertySymbol -> {
                            if (symbol.owner.isDelegated) irCall(symbol.owner.getter!!)
                            else irGetField(null, symbol.owner.backingField!!)
                        }
                        is IrClassSymbol -> {
                            // For classes that are objects, add reference to it
                            if (symbol.owner.isObject) irGetObject(symbol)
                            else null
                        }
                        else -> null
                    }
                }
                val arrayOfCall = irCall(findArrayOfSymbol(pluginContext))
                arrayOfCall.typeArguments[0] = navDestinationType.defaultType
                arrayOfCall.arguments[0] = irVararg(navDestinationType.defaultType, args)
                +irReturn(arrayOfCall)
            }
        }
    }

    private fun findArrayOfSymbol(pluginContext: IrPluginContext): IrSimpleFunctionSymbol {
        val callableId = CallableId(FqName("kotlin"), Name.identifier("arrayOf"))
        // find `arrayOf<T>(vararg elements: T)`
        return pluginContext.referenceFunctions(callableId).singleOrNull {
            val params = it.owner.parameters
            params.size == 1 && params[0].isVararg
        } ?: error("arrayOf function not found")
    }

    private fun isValidAnnotationTarget(symbol: IrSymbol, pluginContext: IrPluginContext): Boolean {
        val navDestinationType = pluginContext.referenceClass(
            ClassId(
                FqName("com.composegears.tiamat.navigation"),
                Name.identifier("NavDestination")
            )
        )!!
        return when (symbol) {
            is IrPropertySymbol -> {
                if (symbol.owner.isDelegated) {
                    symbol.owner.getter!!.returnType.classOrNull?.isSubtypeOfClass(navDestinationType) ?: false
                } else {
                    symbol.owner.backingField!!.type.isSubtypeOfClass(navDestinationType)
                }
            }
            is IrClassSymbol -> {
                // For classes that are objects, add reference to it
                if (symbol.owner.isObject) symbol.isSubtypeOfClass(navDestinationType)
                else false
            }
            else -> false
        }
    }

    private fun IrClass.isSubclassOf(classId: ClassId, context: IrPluginContext): Boolean {
        val classSymbol = context.referenceClass(classId) ?: return false
        //if (this.symbol == classSymbol) return true
        return getSuperClasses().any { it.symbol == classSymbol }
    }

    private fun IrClass.getSuperClasses(): List<IrClass> {
        return superTypes.mapNotNull { it.classOrNull?.owner }
    }
}