package com.composegears.tiamat

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
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

fun SemanticsNodeInteraction.readText() = this
    .fetchSemanticsNode()
    .config
    .getOrNull(SemanticsProperties.Text)
    ?.firstOrNull()
    ?.text