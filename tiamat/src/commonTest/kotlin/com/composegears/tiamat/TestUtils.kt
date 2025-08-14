package com.composegears.tiamat

import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavDestination
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry

fun createTestNavController(
    key: String? = null,
    saveable: Boolean = true,
    parent: NavController? = null,
    startDestination: NavDestination<*>? = null,
) = NavController.create(
    key = key,
    saveable = saveable,
    parent = parent,
    startEntry = startDestination?.toNavEntry(),
    config = {}
)