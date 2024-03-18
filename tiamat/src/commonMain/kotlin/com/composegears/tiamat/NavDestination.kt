package com.composegears.tiamat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A NavDestinationScope provides a scope for the children of NavDestination<Args> entity
 */
@Stable
abstract class NavDestinationScope<Args> internal constructor() {
    internal abstract val navEntry: NavEntry
    abstract val destination: NavDestination<Args>
}

/**
 * Internal NavDestinationScope impl
 */
internal open class NavDestinationScopeImpl<Args>(
    override val navEntry: NavEntry,
    override val destination: NavDestination<Args>
) : NavDestinationScope<Args>()

/**
 * Destination base interface
 *
 * Used to create nav destination instance
 *
 * ```
 * // Example 1
 * val Screen by navDestination<ScreenArgs> {
 *      val args: ScreenArgs = navArgs()
 *      Text(text = "Screen data=${args}")
 * }
 *
 * // Example 2
 * object Screen : NavDestination<ScreenArgs> {
 *     override val name: String = "Screen"
 *
 *     @Composable
 *     override fun NavDestinationScope<ScreenArgs>.Content() {
 *         val args: ScreenArgs = navArgs()
 *         Text(text = "Screen data=${args}")
 *     }
 * }
 *
 * // Example 3
 * val Screen = NavDestination<ScreenArgs>("Screen") {
 *      val args: ScreenArgs = navArgs()
 *      Text(text = "Screen data=${args}")
 * }
 *
 * ```
 */
interface NavDestination<Args> {
    val name: String

    @Composable
    fun NavDestinationScope<Args>.Content()
}

/**
 * internal simple NavDestination impl
 */
internal open class NavDestinationImpl<Args>(
    override val name: String,
    private val content: @Composable NavDestinationScope<Args>.() -> Unit
) : NavDestination<Args> {

    @Composable
    override fun NavDestinationScope<Args>.Content() {
        content()
    }
}

/**
 * Nav destination delegate impl
 */
class NavDestinationInstanceDelegate<Args>(
    private val content: @Composable NavDestinationScope<Args>.() -> Unit
) : ReadOnlyProperty<Nothing?, NavDestination<Args>> {
    private var destination: NavDestination<Args>? = null

    override fun getValue(thisRef: Nothing?, property: KProperty<*>): NavDestination<Args> {
        if (destination == null) destination = NavDestination(property.name, content)
        return destination!!
    }
}

/**
 * NavDestination builder fun
 *
 * @see [NavDestination]
 */
fun <Args> NavDestination(
    name: String,
    content: @Composable NavDestinationScope<Args>.() -> Unit
): NavDestination<Args> =
    NavDestinationImpl(name, content)

/**
 * Nav destination delegate, destination name will be same as property name
 *
 * @param content content builder lambda
 */
fun <Args> navDestination(
    content: @Composable NavDestinationScope<Args>.() -> Unit
) = NavDestinationInstanceDelegate(content)

/**
 * Define the navDestination + it's args
 *
 * @param destination target nav destination
 * @param navArgs destination navArgs
 * @param freeArgs destination freeArgs
 */
data class NavDestinationEntry<Args>(
    val destination: NavDestination<Args>,
    val navArgs: Args? = null,
    val freeArgs: Any? = null
)

/**
 * Convert [NavDestination] into [NavDestinationEntry]
 *
 * @param navArgs destination navArgs
 * @param freeArgs destination freeArgs
 */
fun <Args> NavDestination<Args>.toEntry(navArgs: Args? = null, freeArgs: Any? = null) =
    NavDestinationEntry(destination = this, navArgs = navArgs, freeArgs = freeArgs)

/**
 * Convert [NavDestination] into [NavDestinationEntry]
 *
 * @param navArgs destination navArgs
 */
infix fun <Args> NavDestination<Args>.navArgs(navArgs: Args?) =
    NavDestinationEntry(destination = this, navArgs = navArgs, freeArgs = null)

/**
 * Convert [NavDestination] into [NavDestinationEntry]
 *
 * @param freeArgs destination freeArgs
 */
infix fun <Args> NavDestination<Args>.freeArgs(freeArgs: Any?) =
    NavDestinationEntry(destination = this, navArgs = null, freeArgs = freeArgs)

/**
 * Update or set [NavDestinationEntry] navArgs
 *
 * @param navArgs new navArgs value
 */
infix fun <Args> NavDestinationEntry<Args>.navArgs(navArgs: Args?) =
    this.copy(navArgs = navArgs)

/**
 * Update or set [NavDestinationEntry] freeArgs
 *
 * @param freeArgs new freeArgs value
 */
infix fun <Args> NavDestinationEntry<Args>.freeArgs(freeArgs: Any?) =
    this.copy(freeArgs = freeArgs)
