package com.composegears.tiamat.compose

import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composegears.tiamat.destinations.TiamatGraph
import com.composegears.tiamat.navigation.NavController

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