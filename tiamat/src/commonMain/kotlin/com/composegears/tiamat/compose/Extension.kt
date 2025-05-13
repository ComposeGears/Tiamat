package com.composegears.tiamat.compose

import androidx.compose.runtime.Composable
import com.composegears.tiamat.navigation.NavDestination

/**
 * Extension base interface.
 */
public interface Extension<in Args>

/**
 * Content extension base interface.
 *
 * Default type is [ContentExtension.Type.Overlay]
 */
public interface ContentExtension<in Args> : Extension<Args> {

    @Composable
    public fun NavDestinationScope<out Args>.Content()

    public fun getType(): Type = Type.Overlay

    public enum class Type { Overlay, Underlay }
}

/**
 * Internal simple ContentExtension impl, type = Overlay
 */
internal open class ContentExtensionImpl<in Args>(
    private val content: @Composable NavDestinationScope<out Args>.() -> Unit
) : ContentExtension<Args> {

    @Composable
    override fun NavDestinationScope<out Args>.Content() {
        content()
    }
}

/**
 * Create [ContentExtension.Type.Overlay] content-extension.
 *
 * @param content extension content builder lambda
 */
public fun <Args> extension(
    content: @Composable NavDestinationScope<out Args>.() -> Unit
): Extension<Args> = ContentExtensionImpl(content)

/**
 * Retrieves the first extension of the specified type from the list of extensions.
 *
 * @return The first extension of type [P] if found, or `null` otherwise.
 */
public inline fun <reified P : Extension<*>> NavDestination<*>.ext(): P? =
    (this as? ComposeNavDestination<*>?)?.extensions?.firstOrNull { it is P } as? P?
