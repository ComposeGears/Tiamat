package com.composegears.tiamat

import androidx.compose.runtime.Composable

public abstract class Extension<Args> {

    @Composable
    internal fun extensionContent(scope: NavDestinationScope<Args>) {
        scope.content()
    }

    @Composable
    public abstract fun NavDestinationScope<Args>.content()
}

public inline fun <reified P : Extension<*>> NavDestination<*>.ext(): P? =
    extensions.firstOrNull { it is P } as P?
