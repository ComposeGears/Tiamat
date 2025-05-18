package com.composegears.tiamat.compose

import androidx.compose.runtime.Composable
import com.composegears.tiamat.navigation.NavDestination
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A navigation destination with Compose UI implementation.
 *
 * @param Args The type of arguments this destination accepts
 */
public interface ComposeNavDestination<Args> : NavDestination<Args> {
    /**
     * List of extensions attached to this destination.
     */
    public val extensions: List<NavExtension<Args>>

    /**
     * The UI content of this destination.
     */
    @Composable
    public fun NavDestinationScope<Args>.Content()
}

/**
 * Internal simple ComposeNavDestination impl.
 */
internal open class NavDestinationImpl<Args>(
    override val name: String,
    override val extensions: List<NavExtension<Args>>,
    private val content: @Composable NavDestinationScope<Args>.() -> Unit
) : ComposeNavDestination<Args> {

    @Composable
    override fun NavDestinationScope<Args>.Content() {
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
public class NavDestinationInstanceDelegate<Args>(
    private val extensions: List<NavExtension<Args>>,
    private val content: @Composable NavDestinationScope<Args>.() -> Unit,
) : ReadOnlyProperty<Nothing?, ComposeNavDestination<Args>> {
    private var destination: ComposeNavDestination<Args>? = null

    override fun getValue(thisRef: Nothing?, property: KProperty<*>): ComposeNavDestination<Args> {
        if (destination == null) destination = NavDestinationImpl(property.name, extensions, content)
        return destination!!
    }
}

/**
 * Creates a ComposeNavDestination instance.
 *
 * @param name The name of the NavDestination
 * @param extensions Optional extensions for the NavDestination
 * @param content The content of the NavDestination
 * @return A ComposeNavDestination instance
 */
@Suppress("FunctionName")
public fun <Args> NavDestination(
    name: String,
    extensions: List<NavExtension<Args>> = emptyList(),
    content: @Composable NavDestinationScope<Args>.() -> Unit
): ComposeNavDestination<Args> = NavDestinationImpl(name, extensions, content)

/**
 * Creates a NavDestinationInstanceDelegate for use with property delegation.
 *
 * This allows for declaring destinations as delegated properties where
 * the property name becomes the destination name.
 *
 * Example usage:
 * ```
 * val Home by navDestination<Unit> {
 *     // destination content here
 * }
 * ```
 *
 * @param extensions Optional extensions for the NavDestination
 * @param content Composable function defining the content of the NavDestination
 * @return A delegate that creates and caches a ComposeNavDestination
 */
public fun <Args> navDestination(
    vararg extensions: NavExtension<Args>? = emptyArray(),
    content: @Composable NavDestinationScope<Args>.() -> Unit
): NavDestinationInstanceDelegate<Args> =
    NavDestinationInstanceDelegate(listOfNotNull(*extensions), content)