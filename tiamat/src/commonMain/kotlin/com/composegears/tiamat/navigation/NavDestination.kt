package com.composegears.tiamat.navigation

public interface NavDestination<Args> : RouteElement {
    public companion object {
        public fun <Args> NavDestination<Args>.toNavEntry(
            navArgs: Args? = null,
            freeArgs: Any? = null,
            navResult: Any? = null
        ): NavEntry<Args> = NavEntry(
            destination = this,
            navArgs = navArgs,
            freeArgs = freeArgs,
            navResult = navResult
        )
    }

    public val name: String
}

internal data class UnresolvedDestination(
    override val name: String
) : NavDestination<Any?>