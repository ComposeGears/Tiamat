package com.composegears.tiamat.compose

import androidx.compose.runtime.Composable
import com.composegears.tiamat.navigation.NavDestination
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

public interface ComposeNavDestination<Args> : NavDestination<Args> {
    public val extensions: List<NavExtension<Args>>

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
 * Nav destination delegate impl.
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
 * NavDestination builder.
 *
 * @param name The name of the NavDestination.
 * @param extensions Optional extensions for the NavDestination.
 * @param content Composable function defining the content of the NavDestination.
 * @return A NavDestination instance.
 */
@Suppress("FunctionName")
public fun <Args> NavDestination(
    name: String,
    extensions: List<NavExtension<Args>> = emptyList(),
    content: @Composable NavDestinationScope<Args>.() -> Unit
): ComposeNavDestination<Args> = NavDestinationImpl(name, extensions, content)

/**
 * NavDestination builder.
 *
 * @param extensions Optional extensions for the NavDestination.
 * @param content Composable function defining the content of the NavDestination.
 * @return A NavDestination delegate instance.
 */
public fun <Args> navDestination(
    vararg extensions: NavExtension<Args>? = emptyArray(),
    content: @Composable NavDestinationScope<Args>.() -> Unit
): NavDestinationInstanceDelegate<Args> =
    NavDestinationInstanceDelegate(listOfNotNull(*extensions), content)