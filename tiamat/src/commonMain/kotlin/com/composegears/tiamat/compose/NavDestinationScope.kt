package com.composegears.tiamat.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Stable
import com.composegears.tiamat.navigation.NavEntry

/**
 * Scope for composable content within a navigation destination.
 *
 * Provides access to the current navigation entry and animated visibility scope.
 * Used as the receiver for destination content composable functions.
 *
 * @param Args The type of arguments this destination accepts
 */
@Stable
public abstract class NavDestinationScope<Args> internal constructor() : AnimatedVisibilityScope {
    /**
     * The current navigation entry.
     */
    internal abstract val navEntry: NavEntry<Args>
}

internal open class NavDestinationScopeImpl<Args>(
    override val navEntry: NavEntry<Args>,
    private val animatedVisibilityScope: AnimatedVisibilityScope
) : NavDestinationScope<Args>(),
    AnimatedVisibilityScope by animatedVisibilityScope