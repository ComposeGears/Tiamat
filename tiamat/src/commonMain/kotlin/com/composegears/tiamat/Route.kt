package com.composegears.tiamat

// TODO add doc

/**
 * Navigation route
 */
public class Route private constructor(
    internal val forceReplace: Boolean,
    internal val throwOnFail: Boolean,
    elements: List<Element> = emptyList()
) {
    internal val elements = ArrayList(elements)

    public companion object {

        // ------------- builders -------------

        public fun build(
            vararg elements: Element
        ): Route = build(
            forceReplace = false,
            throwOnFail = false,
            elements = elements
        )

        public fun build(
            forceReplace: Boolean = true,
            throwOnFail: Boolean = false,
            vararg elements: Element
        ): Route = Route(
            forceReplace = forceReplace,
            throwOnFail = throwOnFail,
            elements = elements.toList()
        )

        public fun build(
            builder: RouteBuilderScope.() -> Unit
        ): Route = build(
            forceReplace = false,
            throwOnFail = false,
            builder = builder
        )

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
         * Resolve route element to list of navigation entries
         *
         * May return empty list in case we only need to select nav controller
         *
         * @return list of navigation entries or null if route element is not resolved
         */
        internal fun Element.resolve(nc: NavController): List<NavEntry<*>>? = when (this) {
            is RouteNavController -> if (this.selector(nc)) emptyList() else null
            is NavDestination<*> -> if (nc.isKnownDestination(this)) listOf(this.toNavEntry()) else null
            is NavEntry<*> -> if (nc.isKnownDestination(this.destination)) listOf(this) else null
            is RouteEntries -> {
                val entries = this.entriesProvider(nc)
                if (entries != null && entries.all { nc.isKnownDestination(it.destination) }) entries else null
            }
            else -> null
        }
    }

    public fun clone(): Route = clone(0)

    internal fun clone(drop: Int): Route = Route(
        forceReplace = forceReplace,
        throwOnFail = throwOnFail,
        elements = ArrayList(elements.drop(drop))
    )

    // ------------- helpers -------------

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

    public class RouteBuilderScope internal constructor(private val route: Route) {

        // --------- route destination builders -------------

        public fun route(
            name: String,
            description: String = "name = $name",
        ): Unit = route(
            description = description,
            entryProvider = { nc -> nc.findDestination { it.name == name }?.toNavEntry() },
        )

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

        public fun route(
            navEntry: NavEntry<*>,
            description: String = "name = ${navEntry.destination.name}",
        ): Unit = route(
            description = description,
            entryProvider = { nc -> nc.findDestination { it === navEntry.destination }?.let { navEntry } },
        )

        public fun route(
            description: String = "",
            entryProvider: (navController: NavController) -> NavEntry<*>?,
        ) {
            route.elements += RouteEntries(
                description = description,
                entriesProvider = { nc -> entryProvider(nc)?.let { listOf(it) } }
            )
        }

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

        public fun selectNavController() {
            route.elements += RouteNavController("Any") { true }
        }

        public fun selectNavController(key: String?) {
            route.elements += RouteNavController("key = $key") { it.key == key }
        }
    }
}