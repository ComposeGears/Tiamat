package com.composegears.tiamat.compose

import androidx.compose.runtime.Stable
import com.composegears.tiamat.navigation.NavEntry

/**
 * Scope for composable content within a navigation destination.
 *
 * Provides access to the current navigation entry.
 *
 * @param Args The type of arguments this destination accepts
 */
@Stable
public abstract class NavDestinationScope<Args : Any> internal constructor() {
    /**
     * The current navigation entry.
     */
    internal abstract val navEntry: NavEntry<Args>
}

internal open class NavDestinationScopeImpl<Args : Any>(
    override val navEntry: NavEntry<Args>,
) : NavDestinationScope<Args>()