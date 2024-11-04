package com.composegears.tiamat

import androidx.compose.runtime.Composable

/**
 * Extension base class
 */
public abstract class Extension<Args> {

    @Composable
    internal fun ExtensionContent(scope: NavDestinationScope<Args>) {
        scope.content()
    }

    /**
     * The content of extension
     */
    @Composable
    public abstract fun NavDestinationScope<Args>.content()
}

/**
 * Provides an attached extension of defined type
 *
 * @return extension or null if the ext of this type is not attached
 */
public inline fun <reified P : Extension<*>> NavDestination<*>.ext(): P? =
    extensions.firstOrNull { it is P } as? P?
