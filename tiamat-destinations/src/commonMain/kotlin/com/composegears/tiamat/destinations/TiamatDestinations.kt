package com.composegears.tiamat.destinations

import com.composegears.tiamat.NavDestination

public interface TiamatDestinations {
    public fun items(): Array<NavDestination<*>> = error("Not implemented")
}