package com.composegears.tiamat.destinations

import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.navigation.NavDestination

@SubclassOptInRequired(TiamatExperimentalApi::class)
public interface TiamatGraph {
    public fun destinations(): Array<NavDestination<*>> = error("Graph is empty")

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