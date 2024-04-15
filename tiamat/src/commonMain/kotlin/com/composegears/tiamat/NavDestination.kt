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
    internal abstract val navEntry: NavEntry<Args>
}

/**
 * Internal NavDestinationScope impl
 */
internal open class NavDestinationScopeImpl<Args>(
    override val navEntry: NavEntry<Args>,
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