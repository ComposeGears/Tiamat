package com.composegears.tiamat.navigation

/**
 * Interface for elements that can be part of a navigation route.
 */
public sealed interface RouteElement

/**
 * Represents a navigation route consisting of multiple route elements.
 *
 * @param elements Initial list of route elements
 */
public class Route(elements: List<RouteElement>) {

    internal val elements = elements.toMutableList()

    /**
     * Creates a new route with the specified builder function.
     *
     * @param builder Function to build the route
     */
    public constructor(builder: Route.() -> Unit) : this(emptyList()) {
        builder()
    }

    /**
     * Adds a route element to this route.
     *
     * @param routeElement The element to add
     */
    public fun element(routeElement: RouteElement) {
        elements.add(routeElement)
    }

    /**
     * Adds a destination by name to this route.
     *
     * @param name The name of the destination
     */
    public fun destination(name: String) {
        elements.add(Destination(name))
    }

    /**
     * Adds a navigation controller to this route.
     *
     * @param key The key of the navigation controller
     */
    public fun navController(key: String, saveable: Boolean? = null) {
        elements.add(NavController(key, saveable))
    }

    internal data class Destination(val name: String) : RouteElement

    internal data class NavController(val key: String, val saveable: Boolean? = null) : RouteElement
}
