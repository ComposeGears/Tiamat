package com.composegears.tiamat.compose

import androidx.compose.runtime.Composable
import com.composegears.tiamat.navigation.NavDestination

/**
 * Extension base interface.
 *
 * NavExtensions provide additional functionality to navigation destinations.
 *
 * @param Args The type of arguments the destination accepts
 */
public interface NavExtension<in Args>

/**
 * Content extension base interface.
 *
 * Allows adding additional UI content to a navigation destination.
 * Default type is [ContentExtension.Type.Overlay]
 *
 * @param Args The type of arguments the destination accepts
 */
public interface ContentExtension<in Args> : NavExtension<Args> {

    /**
     * Provides the content to be rendered for this extension.
     *
     * This method is called within the context of a NavDestinationScope.
     */
    @Composable
    public fun NavDestinationScope<out Args>.Content()

    /**
     * Returns the type of content extension.
     *
     * @return The type of the content extension, defaults to [Type.Overlay]
     */
    public fun getType(): Type = Type.Overlay

    /**
     * Defines how the extension content should be positioned relative to the destination content.
     */
    public enum class Type {
        /**
         * Content will be rendered on top of the destination content
         */
        Overlay,

        /**
         * Content will be rendered behind the destination content
         */
        Underlay
    }
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
 * @param content Extension content builder lambda
 * @return A new content extension
 */
public fun <Args> extension(
    content: @Composable NavDestinationScope<out Args>.() -> Unit
): NavExtension<Args> = ContentExtensionImpl(content)

/**
 * Retrieves the list of extensions associated with this navigation destination.
 *
 * @return The list of extensions, or `null` if the destination does not support extensions.
 */
public fun NavDestination<*>.extensions(): List<NavExtension<*>>? =
    (this as? ComposeNavDestination<*>?)?.extensions

/**
 * Retrieves the first extension of the specified type from the list of extensions.
 *
 * @return The first extension of type [P] if found, or `null` otherwise.
 */
public inline fun <reified P : NavExtension<*>> NavDestination<*>.ext(): P? =
    this.extensions()?.firstOrNull { it is P } as? P?
