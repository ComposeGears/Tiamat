package com.composegears.tiamat.navigation

public sealed interface RouteElement

public class Route(elements: List<RouteElement>) {

    internal val elements = elements.toMutableList()

    public constructor(builder: Route.() -> Unit) : this(emptyList()) {
        builder()
    }

    public fun element(routeElement: RouteElement) {
        elements.add(routeElement)
    }

    public fun destination(name: String) {
        elements.add(Destination(name))
    }

    public fun navController(key: String) {
        elements.add(NavController(key))
    }

    internal data class Destination(val name: String) : RouteElement

    internal data class NavController(val key: String, val saveable: Boolean? = null) : RouteElement
}
