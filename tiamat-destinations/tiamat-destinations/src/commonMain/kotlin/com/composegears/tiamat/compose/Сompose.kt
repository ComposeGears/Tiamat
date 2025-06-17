package com.composegears.tiamat.compose

import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composegears.tiamat.destinations.TiamatGraph
import com.composegears.tiamat.navigation.NavController

/**
 * The main navigation composable that displays the current destination and handles transitions.
 *
 * @param navController The NavController to use for navigation
 * @param graph The navigation graph to use
 * @param modifier Modifier to apply to the navigation container
 * @param handleSystemBackEvent Whether to handle system back events (default: true)
 * @param contentTransformProvider Provider function for content transitions based on navigation direction
 */
@Composable
public fun Navigation(
    navController: NavController,
    graph: TiamatGraph,
    modifier: Modifier = Modifier,
    handleSystemBackEvent: Boolean = true,
    contentTransformProvider: (isForward: Boolean) -> ContentTransform = { navigationFadeInOut() },
) {
    Navigation(
        navController = navController,
        destinations = graph.destinations(),
        modifier = modifier,
        handleSystemBackEvent = handleSystemBackEvent,
        contentTransformProvider = contentTransformProvider
    )
}