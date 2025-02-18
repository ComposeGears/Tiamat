package com.composegears.tiamat

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A NavDestinationScope provides a scope for the children of NavDestination<Args> entity.
 */
@Stable
public abstract class NavDestinationScope<Args> internal constructor() : AnimatedVisibilityScope {
    internal abstract val navEntry: NavEntry<Args>
}

/**
 * Internal NavDestinationScope impl.
 */
internal open class NavDestinationScopeImpl<Args>(
    override val navEntry: NavEntry<Args>,
    private val animatedVisibilityScope: AnimatedVisibilityScope
) : NavDestinationScope<Args>(),
    AnimatedVisibilityScope by animatedVisibilityScope

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
public interface NavDestination<Args> : Route.Element {
    public val name: String
    public val extensions: List<Extension<Args>>

    public companion object {
        internal val Stub: NavDestination<Unit> = NavDestinationImpl("Stub", emptyList()) {}
    }

    @Composable
    public fun NavDestinationScope<Args>.Content()
}

/**
 * Internal simple NavDestination impl.
 */
internal open class NavDestinationImpl<Args>(
    override val name: String,
    override val extensions: List<Extension<Args>>,
    private val content: @Composable NavDestinationScope<Args>.() -> Unit
) : NavDestination<Args> {

    @Composable
    override fun NavDestinationScope<Args>.Content() {
        content()
    }
}

/**
 * Nav destination delegate impl.
 */
public class NavDestinationInstanceDelegate<Args>(
    private val extensions: List<Extension<Args>>,
    private val content: @Composable NavDestinationScope<Args>.() -> Unit,
) : ReadOnlyProperty<Nothing?, NavDestination<Args>> {
    private var destination: NavDestination<Args>? = null

    override fun getValue(thisRef: Nothing?, property: KProperty<*>): NavDestination<Args> {
        if (destination == null) destination = NavDestination(property.name, extensions, content)
        return destination!!
    }
}

/**
 * NavDestination builder.
 *
 * @param name The name of the NavDestination.
 * @param extensions Optional extensions for the NavDestination.
 * @param content Composable function defining the content of the NavDestination.
 * @return A NavDestination instance.
 */
public fun <Args> NavDestination(
    name: String,
    extensions: List<Extension<Args>> = emptyList(),
    content: @Composable NavDestinationScope<Args>.() -> Unit
): NavDestination<Args> = NavDestinationImpl(name, extensions, content)

/**
 * NavDestination builder.
 *
 * @param extensions Optional extensions for the NavDestination.
 * @param content Composable function defining the content of the NavDestination.
 * @return A NavDestination delegate instance.
 */
public fun <Args> navDestination(
    vararg extensions: Extension<Args>? = emptyArray(),
    content: @Composable NavDestinationScope<Args>.() -> Unit
): NavDestinationInstanceDelegate<Args> = NavDestinationInstanceDelegate(listOfNotNull(*extensions), content)