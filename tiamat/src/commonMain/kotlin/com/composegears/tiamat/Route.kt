package com.composegears.tiamat

import androidx.compose.runtime.LaunchedEffect

/**
 * Navigation route
 */
public class Route private constructor(
    internal val autoPath: Boolean,
    internal val throwOnFail: Boolean,
    actions: List<Element> = emptyList()
) {
    internal val actions = ArrayList(actions)

    public companion object {

        public fun build(
            vararg elements: Element
        ): Route = build(
            autoPath = true,
            throwOnFail = false,
            elements = elements
        )

        public fun build(
            autoPath: Boolean,
            throwOnFail: Boolean,
            vararg elements: Element
        ): Route = Route(autoPath, throwOnFail, elements.toList())

        public fun build(
            builder: RouteBuilderScope.() -> Unit
        ): Route = build(
            autoPath = true,
            throwOnFail = false,
            builder = builder
        )

        public fun build(
            autoPath: Boolean,
            throwOnFail: Boolean,
            builder: RouteBuilderScope.() -> Unit
        ): Route = Route(
            autoPath = autoPath,
            throwOnFail = throwOnFail,
        ).also { RouteBuilderScope(it).builder() }
    }

    public operator fun plus(route: Route): Route =
        Route(autoPath, throwOnFail, actions + route.actions)

    //------------- helpers -------------

    public sealed interface Element

    internal class RouteDestination<Args>(
        val entryProvider: (navController: NavController) -> NavEntry<Args>?,
        val shouldReplace: (old: NavEntry<Args>, new: NavEntry<Args>) -> Boolean
    ) : Element

    internal class RouteNavController(
        val selector: (NavController) -> Boolean
    ) : Element

    //------------- builder -------------

    public class RouteBuilderScope internal constructor(private val route: Route) {

        //--------- route destination builders -------------

        public fun route(
            name: String,
            entryBuilder: (NavDestination<*>) -> NavEntry<*>? = { it.toNavEntry() },
            shouldReplace: ((old: NavEntry<*>, new: NavEntry<*>) -> Boolean)? = null
        ) {
            route(
                entryProvider = { nc -> nc.findDestination { it.name == name }?.let(entryBuilder) },
                shouldReplace = shouldReplace
            )
        }

        @Suppress("UNCHECKED_CAST")
        public fun <Args> route(
            destination: NavDestination<Args>,
            navArgs: Args? = null,
            freeArgs: Any? = null,
            shouldReplace: ((old: NavEntry<Args>, new: NavEntry<Args>) -> Boolean)? = null
        ) {
            route(
                entryProvider = { nc ->
                    nc.findDestination { it === destination }
                        ?.let { it as NavDestination<Args> }
                        ?.toNavEntry(navArgs, freeArgs)
                },
                shouldReplace = shouldReplace
            )
        }

        public fun <Args> route(
            entryProvider: (navController: NavController) -> NavEntry<Args>?,
            shouldReplace: ((old: NavEntry<Args>, new: NavEntry<Args>) -> Boolean)? = null
        ) {
            route.actions += RouteDestination(
                entryProvider = entryProvider,
                shouldReplace = shouldReplace
                    ?: { old, new -> old.navArgs != new.navArgs || old.freeArgs != new.freeArgs }
            )
        }

        //--------- route nav controller builders -------------

        public fun selectNavController() {
            route.actions += RouteNavController { true }
        }

        public fun selectNavController(key: String?) {
            route.actions += RouteNavController { it.key == key }

        }
    }
}

//-----------todo remove me-------------------

private val S1 by navDestination<Int> {}
private val S2 by navDestination<Unit> {}
private val S3 by navDestination<Unit> {}
private val S4 by navDestination<Unit> {}


@OptIn(TiamatExperimentalApi::class)
private val S by navDestination<Unit> {
    val nc = navController()
    LaunchedEffect(Unit) {

        // simple builder
        val route1 = Route.build(
            autoPath = true,
            throwOnFail = true,
            S1, S2, S3.toNavEntry()
        )

        val fullRoute = Route.build {
            // simple exact dest
            route(S1)
            // exact dest with args, you can also add `shouldReplace(old,new)->Bool` check to define when to skip replacement
            route(S1, 1, null)
            // simple named destination
            route("S1")
            // named dest + entry build logic (eg: you can parse some params to args and pass em)
            route("S1", { (it as NavDestination<Int>).toNavEntry(1, null) })
            // access to navController to resolve a desired nav dest manually
            route({ nc -> nc.findDestination { it.name == "S1" }?.toNavEntry() })

            // ask to step in first nav controller (further nav will be inside this nc stack)
            selectNavController()
            // ask to step in specified (by key) nav controller
            selectNavController("SomeKey")
        }

        // !!! if the screen is not found in the current NavController -> search in the nested !!!

        // option to combine (eg: 2d part comes from outer lib/module)
        val route2 = Route.build(S1, S2) + Route.build(S3, S4)

        // run routing
        nc.route(route2)

    }
}