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

        internal class UnresolvedDestination<Args>(
            override val name: String
        ) : NavDestination<Args>
    }

    public val name: String
}