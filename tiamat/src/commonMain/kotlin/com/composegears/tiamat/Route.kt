package com.composegears.tiamat

/**
 * Navigation route
 */
public class Route private constructor(
    internal val autoPath: Boolean,
    internal val autoSkip: Boolean,
    internal val throwOnFail: Boolean,
    elements: List<Element> = emptyList()
) {
    internal val elements = ArrayList(elements)

    public companion object {

        // ------------- builders -------------

        public fun build(
            vararg elements: Element
        ): Route = build(
            autoPath = true,
            autoSkip = true,
            throwOnFail = false,
            elements = elements
        )

        public fun build(
            autoPath: Boolean = true,
            autoSkip: Boolean = true,
            throwOnFail: Boolean = false,
            vararg elements: Element
        ): Route = Route(
            autoPath = autoPath,
            autoSkip = autoSkip,
            throwOnFail = throwOnFail,
            elements = elements.toList()
        )

        public fun build(
            builder: RouteBuilderScope.() -> Unit
        ): Route = build(
            autoPath = true,
            autoSkip = true,
            throwOnFail = false,
            builder = builder
        )

        public fun build(
            autoPath: Boolean = true,
            autoSkip: Boolean = true,
            throwOnFail: Boolean = false,
            builder: RouteBuilderScope.() -> Unit
        ): Route = Route(
            autoPath = autoPath,
            autoSkip = autoSkip,
            throwOnFail = throwOnFail,
        ).also { RouteBuilderScope(it).builder() }

        // ------------- resolvers -------------

        internal fun Element.isMatchCurrentNavController(nc: NavController) =
            (this as? RouteNavController)
                ?.selector
                ?.invoke(nc)
                ?: false

        internal fun Element.resolveNavEntry(nc: NavController) = when (this) {
            is NavDestination<*> -> takeIf { nc.findDestination { it == this } != null }?.toNavEntry()
            is NavEntry<*> -> takeIf { nc.findDestination { it == this.destination } != null }
            is RouteDestination<*> -> entryProvider(nc)
                ?.takeIf { ne -> nc.findDestination { it == ne.destination } != null }
            else -> null
        }
    }

    public fun clone(): Route = clone(0)

    internal fun clone(drop: Int): Route = Route(
        autoPath = autoPath,
        autoSkip = autoSkip,
        throwOnFail = throwOnFail,
        elements = ArrayList(elements.drop(drop))
    )

    // ------------- helpers -------------

    public sealed interface Element

    internal class RouteDestination<Args>(
        val description: String,
        val entryProvider: (navController: NavController) -> NavEntry<Args>?,
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
            entryBuilder: (NavDestination<*>) -> NavEntry<*>? = { it.toNavEntry() },
        ) {
            route(
                description = description,
                entryProvider = { nc -> nc.findDestination { it.name == name }?.let(entryBuilder) },
            )
        }

        @Suppress("UNCHECKED_CAST")
        public fun <Args> route(
            destination: NavDestination<Args>,
            navArgs: Args? = null,
            freeArgs: Any? = null,
        ) {
            route(
                description = "name = ${destination.name}",
                entryProvider = { nc ->
                    nc.findDestination { it === destination }
                        ?.let { it as NavDestination<Args> }
                        ?.toNavEntry(navArgs, freeArgs)
                },
            )
        }

        public fun <Args> route(
            description: String = "",
            entryProvider: (navController: NavController) -> NavEntry<Args>?,
        ) {
            route.elements += RouteDestination(
                description = description,
                entryProvider = entryProvider,
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