package com.composegears.tiamat.compose

import com.composegears.tiamat.TiamatUnsafeApi
import com.composegears.tiamat.navigation.NavDestination

/**
 * Interface for loading navigation destinations.
 */
public sealed interface DestinationLoader {

    public companion object {

        /**
         * Creates a DestinationLoader that loads destinations from a provided array.
         *
         * @param destinations Array of navigation destinations to search through
         * @return A DestinationLoader that finds destinations by key in the provided array
         */
        public fun from(
            destinations: Array<NavDestination<*>>
        ): DestinationLoader = FromArray(destinations)

        /**
         * Creates a DestinationLoader with a custom loading function.
         *
         * @param loader Function that takes a destination key and returns the corresponding
         *               NavDestination, or null if not found
         * @return A DestinationLoader that uses the provided function to load destinations
         */
        public fun byKey(
            loader: (key: String) -> NavDestination<*>?
        ): DestinationLoader = ByKey(loader)
    }

    /**
     * Loads a navigation destination by its unique key.
     *
     * @param key The unique key of the destination to load
     * @return The loaded NavDestination, or null if not found
     */
    public fun load(key: String): NavDestination<*>?

    /**
     * A DestinationLoader that never loads any destinations.
     *
     * This implementation always returns null for any destination key.
     * Useful for scenarios where destination loading is not required.
     */
    @TiamatUnsafeApi
    public object DoNotLoad : DestinationLoader {
        override fun load(key: String): NavDestination<*>? = null
    }

    /**
     * A DestinationLoader that loads destinations from a predefined array.
     *
     * This implementation searches through the provided array to find
     * destinations that match the requested key.
     *
     * @property destinations Array of available navigation destinations
     */
    public class FromArray(
        private val destinations: Array<NavDestination<*>>
    ) : DestinationLoader {
        override fun load(key: String): NavDestination<*>? =
            destinations.firstOrNull { it.key == key }
    }

    /**
     * A DestinationLoader that uses a custom function to load destinations.
     *
     * This implementation delegates destination loading to the provided function.
     *
     * @property loader Function that loads a destination by key
     */
    public class ByKey(
        private val loader: (key: String) -> NavDestination<*>?
    ) : DestinationLoader {
        override fun load(key: String): NavDestination<*>? = loader(key)
    }
}