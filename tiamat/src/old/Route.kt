package com.composegears.tiamat

/**
 * Represents a navigation route.
 */
public class Route private constructor(
    internal val forceReplace: Boolean,
    internal val throwOnFail: Boolean,
    elements: List<Element> = emptyList()
) {
    internal val elements = ArrayList(elements)

    public companion object {

        // ------------- builders -------------

        /**
         * Builds a new Route with the given elements.
         *
         * @param elements The elements to include in the route.
         * @return A new Route instance.
         */
        public fun build(
            vararg elements: Element
        ): Route = build(
            forceReplace = false,
            throwOnFail = false,
            elements = elements
        )

        /**
         * Builds a new Route with the given parameters.
         *
         * @param forceReplace Whether to force replace the route elements or to skip them.
         * @param throwOnFail Whether to throw an exception on failure.
         * @param elements The elements to include in the route.
         * @return A new Route instance.
         */
        public fun build(
            forceReplace: Boolean = true,
            throwOnFail: Boolean = false,
            vararg elements: Element
        ): Route = Route(
            forceReplace = forceReplace,
            throwOnFail = throwOnFail,
            elements = elements.toList()
        )

        /**
         * Builds a new Route using a builder function.
         *
         * @param builder The builder function to configure the route.
         * @return A new Route instance.
         */
        public fun build(
            builder: RouteBuilderScope.() -> Unit
        ): Route = build(
            forceReplace = false,
            throwOnFail = false,
            builder = builder
        )

        /**
         * Builds a new Route with the given parameters using a builder function.
         *
         * @param forceReplace Whether to force replace the route elements or to skip them.
         * @param throwOnFail Whether to throw an exception on failure.
         * @param builder The builder function to configure the route.
         * @return A new Route instance.
         */
        public fun build(
            forceReplace: Boolean = true,
            throwOnFail: Boolean = false,
            builder: RouteBuilderScope.() -> Unit
        ): Route = Route(
            forceReplace = forceReplace,
            throwOnFail = throwOnFail,
        ).also { RouteBuilderScope(it).builder() }

        // ------------- resolvers -------------

        /**
         * Resolves a route element to a list of navigation entries.
         *
         * May return an empty list in case we only need to select a navigation controller.
         *
         * @param nc The NavController to resolve the element against.
         * @return List of navigation entries or null if the route element is not resolved.
         */
        internal fun Element.resolve(nc: NavController): List<NavEntry<*>>? = when (this) {
            is RouteNavController -> if (this.selector(nc)) emptyList() else null
            is NavDestination<*> -> if (nc.isKnownDestination(this)) listOf(this.toNavEntry()) else null
            is NavEntry<*> -> if (nc.isKnownDestination(this.destination)) listOf(this) else null
            is RouteEntries -> {
                val entries = this.entriesProvider(nc)
                if (entries != null && entries.all { nc.isKnownDestination(it.destination) }) entries else null
            }
        }
    }

    /**
     * Creates a clone of the current Route.
     *
     * @return A new Route instance with the same properties.
     */
    public fun clone(): Route = clone(0)

    /**
     * Creates a clone of the current Route, dropping the specified number of elements.
     *
     * @param drop The number of elements to drop from the beginning.
     * @return A new Route instance with the remaining elements.
     */
    internal fun clone(drop: Int): Route = Route(
        forceReplace = forceReplace,
        throwOnFail = throwOnFail,
        elements = ArrayList(elements.drop(drop))
    )

    // ------------- helpers -------------

    /**
     * Represents an element in the route.
     */
    public sealed interface Element

    internal class RouteEntries(
        val description: String,
        val entriesProvider: (navController: NavController) -> List<NavEntry<*>>?,
    ) : Element

    internal class RouteNavController(
        val description: String,
        val selector: (NavController) -> Boolean,
    ) : Element

    // ------------- builder -------------

    /**
     * Scope for building a Route.
     *
     * @property route The Route being built.
     */
    public class RouteBuilderScope internal constructor(private val route: Route) {

        // --------- route destination builders -------------

        /**
         * Adds a route destination by name.
         *
         * @param name The name of the destination.
         * @param description The description of the destination.
         */
        public fun route(
            name: String,
            description: String = "name = $name",
        ): Unit = route(
            description = description,
            entryProvider = { nc -> nc.findDestination { it.name == name }?.toNavEntry() },
        )

        /**
         * Adds a route destination with arguments.
         *
         * @param destination The destination to add.
         * @param navArgs The navigation navArgs.
         * @param freeArgs The navigation freeArgs.
         */
        @Suppress("UNCHECKED_CAST")
        public fun <Args> route(
            destination: NavDestination<Args>,
            navArgs: Args? = null,
            freeArgs: Any? = null,
        ): Unit = route(
            description = "name = ${destination.name}",
            entryProvider = { nc ->
                nc.findDestination { it === destination }
                    ?.let { it as NavDestination<Args> }
                    ?.toNavEntry(navArgs, freeArgs)
            },
        )

        /**
         * Adds a route destination with a navigation entry.
         *
         * @param navEntry The navigation entry to add.
         * @param description The description of the destination.
         */
        public fun route(
            navEntry: NavEntry<*>,
            description: String = "name = ${navEntry.destination.name}",
        ): Unit = route(
            description = description,
            entryProvider = { nc -> nc.findDestination { it === navEntry.destination }?.let { navEntry } },
        )

        /**
         * Adds a route destination with a custom entry provider.
         *
         * @param description The description of the destination.
         * @param entryProvider The function to provide the navigation entry.
         */
        public fun route(
            description: String = "",
            entryProvider: (navController: NavController) -> NavEntry<*>?,
        ) {
            route.elements += RouteEntries(
                description = description,
                entriesProvider = { nc -> entryProvider(nc)?.let { listOf(it) } }
            )
        }

        /**
         * Adds a list of route destinations with a custom entries provider.
         *
         * @param description The description of the destinations.
         * @param entriesProvider The function to provide the list of navigation entries.
         */
        public fun routeList(
            description: String = "",
            entriesProvider: (navController: NavController) -> List<NavEntry<*>>?,
        ) {
            route.elements += RouteEntries(
                description = description,
                entriesProvider = entriesProvider
            )
        }

        // --------- route nav controller builders -------------

        /**
         * Selects any navigation controller.
         */
        public fun selectNavController() {
            route.elements += RouteNavController("Any") { true }
        }

        /**
         * Selects a navigation controller by key.
         *
         * @param key The key of the navigation controller.
         */
        public fun selectNavController(key: String?) {
            route.elements += RouteNavController("key = $key") { it.key == key }
        }
    }
}