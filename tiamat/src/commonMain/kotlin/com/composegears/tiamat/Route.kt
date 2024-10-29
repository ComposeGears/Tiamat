package com.composegears.tiamat

public class Route private constructor(
    internal val failStrategy: FailStrategy,
    actions: List<RouteItem> = emptyList()
) {
    internal val actions = ArrayList(actions)

    public companion object {
        // todo add doc
        public fun start(
            failStrategy: FailStrategy = FailStrategy.Ignore,
            description: String? = null,
            actions: NavController.() -> Unit
        ): Route = Route(failStrategy).next(description, { true }, actions)
    }

    // todo add doc
    public fun next(
        description: String? = null,
        selector: NavController.() -> Boolean = { true },
        action: NavController.() -> Unit
    ): Route = apply {
        actions.add(RouteItem(description, selector, action))
    }

    internal fun clone() = Route(failStrategy, actions)

    internal data class RouteItem(
        val description: String?,
        val selector: NavController.() -> Boolean = { true },
        val action: NavController.() -> Unit
    )

    public enum class FailStrategy { Ignore, Throw }
}