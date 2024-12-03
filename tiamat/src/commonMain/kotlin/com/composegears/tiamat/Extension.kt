package com.composegears.tiamat

import androidx.compose.runtime.Composable

/**
 * Extension base class
 */
public interface Extension<in Args> {
    @Composable
    public fun NavDestinationScope<out Args>.Content()
}

/**
 * internal simple Extension impl
 */
internal open class ExtensionImpl<in Args>(
    private val content: @Composable NavDestinationScope<out Args>.() -> Unit
) : Extension<Args> {
    @Composable
    override fun NavDestinationScope<out Args>.Content() {
        content()
    }
}

/**
 * Simple extension builder
 *
 * @param content extension content builder lambda
 */
public fun <Args> extension(
    content: @Composable NavDestinationScope<out Args>.() -> Unit
): Extension<Args> = ExtensionImpl(content)

/**
 * Provides an attached extension of defined type
 *
 * @return extension or null if the ext of this type is not attached
 */
public inline fun <reified P : Extension<*>> NavDestination<*>.ext(): P? =
    extensions.firstOrNull { it is P } as? P?
