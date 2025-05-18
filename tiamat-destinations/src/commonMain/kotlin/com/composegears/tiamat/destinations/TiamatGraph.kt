package com.composegears.tiamat.destinations

import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.navigation.NavDestination

/**
 * Represents a navigation graph containing a collection of destinations.
 *
 * Graphs can be created by implementing this interface directly or by using
 * the annotation processor with the [InstallIn] annotation.
 */
@SubclassOptInRequired(TiamatExperimentalApi::class)
public interface TiamatGraph {
    /**
     * Returns all destinations that belong to this graph.
     *
     * @return An array of navigation destinations contained in this graph
     * @throws IllegalStateException if the graph is empty and no implementation is provided
     */
    public fun destinations(): Array<NavDestination<*>> = error("Graph is empty")

    /**
     * Combines this graph with another graph, returning a merged graph that contains
     * unique destinations from both.
     *
     * @param other The graph to combine with this one
     * @return A new merged graph containing destinations from both graphs
     */
    public operator fun plus(other: TiamatGraph): TiamatGraph {
        if (this is TiamatMergedGraph) {
            this.allDestinations.addAll(other.destinations())
            return this
        } else {
            val mergedGraph = TiamatMergedGraph()
            mergedGraph.allDestinations.addAll(this.destinations())
            mergedGraph.allDestinations.addAll(other.destinations())
            return mergedGraph
        }
    }
}

@OptIn(TiamatExperimentalApi::class)
internal class TiamatMergedGraph : TiamatGraph {
    internal val allDestinations = mutableSetOf<NavDestination<*>>()

    override fun destinations(): Array<NavDestination<*>> = allDestinations.toTypedArray()
}