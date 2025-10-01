package com.composegears.tiamat.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.composegears.tiamat.navigation.NavDestination
import com.composegears.tiamat.navigation.NavEntry
import kotlin.jvm.JvmName
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Scope for composable content within a navigation destination.
 *
 * Provides access to the current navigation entry.
 *
 * @param Args The type of arguments this destination accepts
 */
@Stable
@Suppress("UseDataClass")
public class NavDestinationScope<Args : Any> internal constructor(
    /**
     * The current navigation entry.
     */
    @PublishedApi
    internal val navEntry: NavEntry<Args>,
)

/**
 * A navigation destination with Compose UI implementation.
 */
public class ComposeNavDestination<Args : Any> internal constructor(
    name: String,
    argsType: KType,
    /**
     * List of extensions attached to this destination.
     */
    public val extensions: List<NavExtension<Args>>,
    /**
     * Composable function defining the content of this destination.
     */
    private val content: @Composable NavDestinationScope<Args>.() -> Unit
) : NavDestination<Args>(
    name = name,
    argsType = argsType
) {
    /**
     * The UI content of this destination.
     */
    @Composable
    internal fun NavDestinationScope<Args>.Content() {
        content()
    }
}

/**
 * Nav destination delegate implementation.
 *
 * Provides lazy initialization of a ComposeNavDestination when accessed through a delegated property.
 *
 * @param extensions List of extensions for the destination
 * @param content Composable function defining the content of the destination
 */
public class NavDestinationInstanceDelegate<Args : Any>(
    private val argsType: KType,
    private val extensions: List<NavExtension<Args>>,
    private val content: @Composable NavDestinationScope<Args>.() -> Unit,
) : ReadOnlyProperty<Any?, ComposeNavDestination<Args>> {
    private var destination: ComposeNavDestination<Args>? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): ComposeNavDestination<Args> {
        if (destination == null) destination = ComposeNavDestination(
            name = property.name,
            argsType = argsType,
            extensions = extensions,
            content = content
        )
        return destination!!
    }
}

/**
 * Creates a NavDestinationInstanceDelegate for use with property delegation.
 *
 * This allows for declaring destinations as delegated properties where
 * the property name becomes the destination name.
 *
 * Example usage:
 * ```
 * val Home by navDestination {
 *     // destination content here
 * }
 * ```
 *
 * @param extensions Optional extensions for the NavDestination
 * @param content Composable function defining the content of the NavDestination
 * @return A delegate that creates and caches a ComposeNavDestination
 */
@JvmName("unitNavDestination")
public fun navDestination(
    vararg extensions: NavExtension<Unit>? = emptyArray(),
    content: @Composable NavDestinationScope<Unit>.() -> Unit
): NavDestinationInstanceDelegate<Unit> = NavDestinationInstanceDelegate(
    argsType = typeOf<Unit>(),
    extensions = listOfNotNull(*extensions),
    content = content
)

/**
 * Creates a NavDestinationInstanceDelegate for use with property delegation.
 *
 * This allows for declaring destinations as delegated properties where
 * the property name becomes the destination name.
 *
 * Example usage:
 * ```
 * val Home by navDestination<Args> {
 *     // destination content here
 * }
 * ```
 *
 * @param Args The type of arguments this destination accepts
 * @param extensions Optional extensions for the NavDestination
 * @param content Composable function defining the content of the NavDestination
 * @return A delegate that creates and caches a ComposeNavDestination
 */
public inline fun <reified Args : Any> navDestination(
    vararg extensions: NavExtension<Args>? = emptyArray(),
    noinline content: @Composable NavDestinationScope<Args>.() -> Unit
): NavDestinationInstanceDelegate<Args> = NavDestinationInstanceDelegate(
    argsType = typeOf<Args>(),
    extensions = listOfNotNull(*extensions),
    content = content
)