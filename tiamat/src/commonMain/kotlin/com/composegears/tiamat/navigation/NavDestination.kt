package com.composegears.tiamat.navigation

import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Interface for a navigation destination.
 *
 * A navigation destination represents a screen or other UI element
 * that can be navigated to.
 *
 * @param Args The type of arguments this destination accepts
 */
public abstract class NavDestination<Args : Any>(
    /**
     * The unique name of this destination.
     */
    public val name: String,
    /**
     * Args class
     */
    internal val argsType: KType,
) : RouteElement {
    public companion object {
        /**
         * Creates a NavEntry from this destination.
         *
         * @param navArgs Optional typed arguments to pass to the destination
         * @param freeArgs Optional untyped arguments to pass to the destination
         * @param navResult Optional result value for this entry
         * @return A NavEntry representing this destination
         */
        public fun <Args : Any> NavDestination<Args>.toNavEntry(
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
}

internal class UnresolvedDestination(
    name: String
) : NavDestination<Any>(
    name = name,
    argsType = typeOf<Any>()
)