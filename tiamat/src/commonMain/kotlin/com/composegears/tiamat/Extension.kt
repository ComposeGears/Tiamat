package com.composegears.tiamat

import androidx.compose.runtime.Composable

/**
 * Extension base interface
 */
public interface Extension<in Args>

/**
 * Extension with a content base interface
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
 * internal simple ContentExtension impl, type = Overlay
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
 * Simple [ContentExtension.Type.Overlay] content-extension builder
 *
 * @param content extension content builder lambda
 */
public fun <Args> extension(
    content: @Composable NavDestinationScope<out Args>.() -> Unit
): Extension<Args> = ContentExtensionImpl(content)

/**
 * Provides an attached extension of defined type
 *
 * @return extension or null if the ext of this type is not attached
 */
public inline fun <reified P : Extension<*>> NavDestination<*>.ext(): P? =
    extensions.firstOrNull { it is P } as? P?
