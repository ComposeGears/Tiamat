package com.composegears.tiamat

/**
 * Navigation route
 *
 * @property failStrategy the strategy to handle failures during navigation
 * @property actions the list of actions to be performed in this route
 */
public class Route private constructor(
    internal val failStrategy: FailStrategy,
    actions: List<RouteItem> = emptyList()
) {
    internal val actions = ArrayList(actions)

    public companion object {
        /**
         * Starts a new route with the given fail strategy and initial actions.
         *
         * @param failStrategy the strategy to handle failures during navigation. Defaults to `FailStrategy.Ignore`
         * @param description an optional description of the route
         * @param actions the actions to be performed with current nav controller
         * @return A new preconfigured `Route` instance with the specified parameters
         */
        public fun start(
            failStrategy: FailStrategy = FailStrategy.Ignore,
            description: String? = null,
            actions: NavController.() -> Unit
        ): Route = Route(failStrategy).next(description, { true }, actions)
    }

    /**
     * Adds a new action to the route
     *
     * @param description an optional description of the action
     * @param selector a nav controller selector to determine if the action should be performed
     * @param action the action to be performed
     * @return the `Route` instance with the new action added
     */
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