@file:OptIn(FirIncompatiblePluginAPI::class, UnsafeDuringIrConstructionAPI::class)

package com.composegears.tiamat.destinations

import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.isObject
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.Logger

class TiamatDestinationsIrGenerationExtension(val logger: Logger) : IrGenerationExtension {

    private fun unsupportedTypeMessage(message: String) = """
            |[TD] error
            |
            |Unsupported notation: $message
            |
            |Only objects and properties of `NavDestination` type are supported.
            |Annotation param should be `TiamatDestinations` subclass (DO NOT USE `TiamatDestinations` itself)
            |Example:
            |
            |object MyGraph : TiamatDestinations
            |
            |@InstallIn(MyGraph::class)
            |val Screen1 by navDestination<Unit> { }
            |
            |@InstallIn(MyGraph::class)
            |val Screen2 = NavDestination<Unit>(name = "Screen2") {}
            |
            |@InstallIn(MyGraph::class)
            |object Screen3 : NavDestination<Int> {}
            |
            |class Screen4Class : NavDestination<Int>
            |@InstallIn(MyGraph::class)
            |val Screen4 = Screen4Class()
            |
        """.trimMargin()

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

        logger.log("[TD] TiamatDestinationsIrGenerationExtension started")

        val installInAnnotation = FqName("com.composegears.tiamat.destinations.InstallIn")
        val tiamatDestinationsClass = ClassId(
            packageFqName = FqName("com.composegears.tiamat.destinations"),
            topLevelName = Name.identifier("TiamatDestinations")
        )

        // Find all variables and classes annotated with @InstallIn
        val annotatedElements = mutableMapOf<IrClassSymbol, MutableList<IrSymbol>>()

        // Process all declarations in the module
        moduleFragment.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitProperty(declaration: IrProperty): IrStatement {
                declaration.annotations
                    .filter { it.type.classFqName == installInAnnotation }
                    .mapNotNull { it.valueArguments.firstOrNull()?.extractContainerClass()?.symbol }
                    .onEach { annotatedElements.getOrPut(it) { mutableListOf() }.add(declaration.symbol) }
                return super.visitProperty(declaration)
            }

            override fun visitClass(declaration: IrClass): IrStatement {
                declaration.annotations
                    .filter { it.type.classFqName == installInAnnotation }
                    .mapNotNull { it.valueArguments.firstOrNull()?.extractContainerClass()?.symbol }
                    .onEach { annotatedElements.getOrPut(it) { mutableListOf() }.add(declaration.symbol) }
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

        // Types validation
        listOfNotNull(
            annotatedElements
                .map { it.key }
                .filter { !it.owner.isSubclassOf(tiamatDestinationsClass, pluginContext) }
                .takeIf { it.isNotEmpty() }
                ?.joinToString(
                    prefix = "Annotation value: [\n",
                    separator = "\n",
                    postfix = "\n]",
                    transform = { "    $it" }
                ),
            annotatedElements
                .flatMap { it.value }
                .filter { !isValidAnnotationTarget(it, pluginContext) }
                .takeIf { it.isNotEmpty() }
                ?.joinToString(
                    prefix = "Annotation target: [\n",
                    separator = "\n",
                    postfix = "\n]",
                    transform = { "    $it" }
                ))
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "\n")
            ?.let { error(unsupportedTypeMessage(it)) }

        // Log info
        logger.log("[TD] Destinations map:")
        annotatedElements.forEach { (targetClass, elements) ->
            logger.log("[TD] ${targetClass.owner.name}: [")
            elements.forEach { element ->
                logger.log("[TD]   $element")
            }
            logger.log("[TD] ]")
        }

        // For each TiamatDestinations class, override the items() function
        moduleFragment.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitClass(declaration: IrClass): IrStatement {
                if (declaration.isSubclassOf(tiamatDestinationsClass, pluginContext)) {
                    logger.log("[TD] Found ${declaration.name}")
                    val annotatedForThisClass = annotatedElements[declaration.symbol]
                    if (!annotatedForThisClass.isNullOrEmpty()) {
                        // Create or override the items() function
                        logger.log("[TD] Modifying ${declaration.name}::items() function")
                        addOrReplaceItemsFunction(declaration, annotatedForThisClass, pluginContext)
                    }
                }

                return super.visitClass(declaration)
            }
        })
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun addOrReplaceItemsFunction(
        irClass: IrClass,
        elements: List<IrSymbol>,
        pluginContext: IrPluginContext
    ) {
        val navDestinationType = pluginContext.referenceClass(
            ClassId(FqName("com.composegears.tiamat"), Name.identifier("NavDestination"))
        )!!

        // Remove defined function (cover from those who may try to override by their own)
        irClass
            .declarations
            .filterIsInstance<IrSimpleFunction>()
            .find { it.name.asString() == "items" }
            ?.let { irClass.declarations.remove(it) }

        // Create mew `items` function
        irClass.addFunction(
            name = "items",
            modality = Modality.OPEN,
            returnType = pluginContext.irBuiltIns.arrayClass.typeWith(navDestinationType.defaultType)
        ).apply {
            // Build the function body that returns array of NavDestination objects
            val irBuilder = DeclarationIrBuilder(pluginContext, this.symbol)

            body = irBuilder.irBlockBody {
                val arrayOfCall = irCall(
                    callee = pluginContext.referenceFunctions(
                        CallableId(FqName("kotlin"), Name.identifier("arrayOf"))
                    ).first(),
                    type = pluginContext.irBuiltIns.arrayClass.typeWith(navDestinationType.defaultType)
                )
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

                arrayOfCall.putTypeArgument(0, navDestinationType.defaultType)
                arrayOfCall.putValueArgument(0, irVararg(navDestinationType.defaultType, args))
                +irReturn(arrayOfCall)
            }
        }
    }

    private fun isValidAnnotationTarget(symbol: IrSymbol, pluginContext: IrPluginContext): Boolean {
        val navDestinationType = pluginContext.referenceClass(
            ClassId(FqName("com.composegears.tiamat"), Name.identifier("NavDestination"))
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