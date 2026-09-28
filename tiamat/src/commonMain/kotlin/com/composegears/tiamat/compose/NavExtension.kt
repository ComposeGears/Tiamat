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
 *
 * Implementations must call `entryContent()` to render the destination's main UI;
 * otherwise the destination content will not be displayed.
 *
 * @param Args The type of arguments the destination accepts
 */
public interface ContentExtension<in Args : Any> : NavExtension<Args> {

    /**
     * Renders extension content in the scope of the current destination.
     *
     * @param entryContent The destination's primary content that the extension must wrap,
     *   decorate, or invoke as part of its rendering.
     */
    @Composable
    public fun NavDestinationScope<out Args>.Content(
        entryContent: @Composable () -> Unit
    )
}

/**
 * Combines a list of content extensions into a single composable function that wraps the destination content.
 */
internal fun <T : Any> List<ContentExtension<T>>.combine():
    @Composable NavDestinationScope<out T>.(@Composable () -> Unit) -> Unit =
    { body ->
        var wrappedContent: @Composable () -> Unit = body
        forEach { ext ->
            val previousContent = wrappedContent
            wrappedContent = {
                with(ext) {
                    Content(previousContent)
                }
            }
        }
        wrappedContent()
    }

/**
 * Internal simple ContentExtension impl, type = Overlay
 */
internal open class ContentExtensionImpl<in Args : Any>(
    private val content: @Composable NavDestinationScope<out Args>.(
        entryContent: @Composable () -> Unit
    ) -> Unit
) : ContentExtension<Args> {

    @Composable
    override fun NavDestinationScope<out Args>.Content(
        entryContent: @Composable () -> Unit
    ) {
        content(entryContent)
    }
}

/**
 * Create a simple content-extension.
 *
 * The provided lambda is responsible for rendering the destination UI and must
 * invoke `entryContent()` exactly once.
 *
 * @param content Extension content builder lambda.
 * @return A new content extension
 */
public fun <Args : Any> extension(
    content: @Composable NavDestinationScope<out Args>.(
        entryContent: @Composable () -> Unit
    ) -> Unit
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
