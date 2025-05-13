package com.composegears.tiamat.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Stable
import com.composegears.tiamat.navigation.NavEntry

@Stable
public abstract class NavDestinationScope<Args> internal constructor() : AnimatedVisibilityScope {
    internal abstract val navEntry: NavEntry<Args>
}

/**
 * Internal NavDestinationScope impl.
 */
internal open class NavDestinationScopeImpl<Args>(
    override val navEntry: NavEntry<Args>,
    private val animatedVisibilityScope: AnimatedVisibilityScope
) : NavDestinationScope<Args>(),
    AnimatedVisibilityScope by animatedVisibilityScope