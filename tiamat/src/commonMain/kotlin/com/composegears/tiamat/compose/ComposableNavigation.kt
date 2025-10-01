package com.composegears.tiamat.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.input.pointer.pointerInput
import com.composegears.tiamat.compose.TransitionController.Event.*
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavDestination
import com.composegears.tiamat.navigation.NavEntry
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf

/**
 * CompositionLocal that provides current [AnimatedVisibilityScope].
 */
public val LocalNavAnimatedVisibilityScope: ProvidableCompositionLocal<AnimatedVisibilityScope?> =
    staticCompositionLocalOf { null }

/**
 * The main navigation composable that displays the current destination and handles transitions.
 *
 * @param navController The NavController to use for navigation
 * @param destinations Array of available destinations for this navigation
 * @param modifier Modifier to apply to the navigation container
 * @param handleSystemBackEvent Whether to handle system back events (default: true)
 * @param contentTransformProvider Provider function for content transitions based on navigation direction
 */
@Composable
@Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod")
public fun Navigation(
    navController: NavController,
    destinations: Array<NavDestination<*>>,
    modifier: Modifier = Modifier,
    handleSystemBackEvent: Boolean = true,
    contentTransformProvider: (isForward: Boolean) -> ContentTransform = { navigationFadeInOut() },
) {
    Navigation(
        navController = navController,
        destinationResolver = { name -> destinations.firstOrNull { it.name == name } },
        modifier = modifier,
        handleSystemBackEvent = handleSystemBackEvent,
        contentTransformProvider = contentTransformProvider
    )
}

/**
 * The main navigation composable that displays the current destination and handles transitions.
 *
 * @param navController The NavController to use for navigation
 * @param destinationResolver A function that resolves a destination by name (used during restoration from saved state)
 * @param modifier Modifier to apply to the navigation container
 * @param handleSystemBackEvent Whether to handle system back events (default: true)
 * @param contentTransformProvider Provider function for content transitions based on navigation direction
 */
@Composable
@Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod")
public fun Navigation(
    navController: NavController,
    destinationResolver: (name: String) -> NavDestination<*>?,
    modifier: Modifier = Modifier,
    handleSystemBackEvent: Boolean = true,
    contentTransformProvider: (isForward: Boolean) -> ContentTransform = { navigationFadeInOut() },
) {
    NavigationScene(
        navController = navController,
        destinationResolver = destinationResolver,
        handleSystemBackEvent = handleSystemBackEvent,
    ) {
        val stubEntry = remember {
            NavEntry(
                ComposeNavDestination(
                    name = "Stub",
                    argsType = typeOf<Unit>(),
                    extensions = emptyList(),
                    content = {}
                )
            )
        }
        val navState by navController.navStateFlow.collectAsState()
        val currentScreen by remember(navState) { derivedStateOf { navState.stack.lastOrNull() } }

        // 1) we only launch transition when screen changes
        // 2) seekable transition has a bug when one of props is `null`, so we will use stub destination instead of `null`
        val transitionTarget = remember(currentScreen) { currentScreen ?: stubEntry }
        val transitionData = remember(currentScreen) { navState.transitionData as? TransitionData }
        val transitionState = remember { SeekableTransitionState<NavEntry<*>>(stubEntry) }
        // state/transition controller
        LaunchedEffect(currentScreen) {
            val controller = transitionData?.transitionController
            if (controller != null) {
                controller
                    .updates
                    .transformWhile { item ->
                        emit(item)
                        item is Update
                    }
                    .collect { item ->
                        when (item) {
                            is Cancel -> {
                                animate(transitionState.fraction, 0f, 0f, item.animationSpec) { v, _ ->
                                    this@LaunchedEffect.launch {
                                        transitionState.seekTo(v)
                                    }
                                }
                                transitionState.snapTo(transitionState.currentState)
                                navController.navigate(
                                    entry = transitionState.currentState,
                                    transition = navigationNone()
                                )
                            }
                            is Finish -> transitionState.animateTo(transitionTarget, item.animationSpec)
                            is Update -> transitionState.seekTo(item.value, transitionTarget)
                        }
                    }
            } else transitionState.animateTo(transitionTarget)
        }
        // content
        val transition = rememberTransition(transitionState)
        var contentZIndex by remember { mutableFloatStateOf(0f) }
        transition.AnimatedContent(
            contentKey = { it.contentKey() },
            contentAlignment = Alignment.Center,
            modifier = modifier,
            transitionSpec = {
                val transitionType = navState.transitionType
                val isNoAnimation = transition.targetState == stubEntry ||
                    transition.currentState == stubEntry ||
                    transitionType == NavController.TransitionType.Instant
                val transform = when {
                    isNoAnimation -> navigationNone()
                    transitionData?.contentTransform != null -> transitionData.contentTransform
                    else -> contentTransformProvider(transitionType == NavController.TransitionType.Forward)
                }
                contentZIndex += transform.targetContentZIndex
                ContentTransform(
                    targetContentEnter = transform.targetContentEnter,
                    initialContentExit = transform.initialContentExit,
                    sizeTransform = transform.sizeTransform,
                    targetContentZIndex = contentZIndex,
                )
            },
        ) {
            if (it != stubEntry) CompositionLocalProvider(
                LocalNavAnimatedVisibilityScope provides this
            ) {
                Box {
                    EntryContent(it)
                    if (it != transitionTarget && transition.isRunning) Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        event.changes.forEach { e ->
                                            e.consume()
                                        }
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}

/**
 * Provides a customizable way to display navigation content managed by a [NavController].
 *
 * `NavigationScene` offers a [NavigationSceneScope] that allows for fine-grained control over
 * how and where individual navigation entries are rendered using [NavigationSceneScope.EntryContent].
 * This is particularly useful for implementing custom layouts (e.g., side-by-side panes, lists with details) or
 * complex animated transitions that are not covered by the standard [Navigation] composable.
 *
 * Example usage:
 * ```
 * NavigationScene(navController, destinations) { // this: NavigationSceneScope
 *     val currentEntry by navController.currentNavEntryAsState()
 *     AnimatedContent(
 *         targetState = currentEntry,
 *         contentKey = { it?.contentKey() },
 *         transitionSpec = { navigationFadeInOut() }
 *     ) {
 *         EntryContent(it) // Renders the content of the current navigation entry
 *     }
 * }
 * ```
 *
 * @param navController The NavController to use for navigation
 * @param destinations Array of available destinations for this navigation
 * @param handleSystemBackEvent Whether to handle system back events (default: true)
 * @param scene Scene builder composable function that defines how navigation entries are rendered
 */
@Composable
@Suppress("CognitiveComplexMethod")
public fun NavigationScene(
    navController: NavController,
    destinations: Array<NavDestination<*>>,
    handleSystemBackEvent: Boolean = true,
    scene: @Composable NavigationSceneScope.() -> Unit
) {
    NavigationScene(
        navController = navController,
        destinationResolver = { name -> destinations.firstOrNull { it.name == name } },
        handleSystemBackEvent = handleSystemBackEvent,
        scene = scene
    )
}

/**
 * Provides a customizable way to display navigation content managed by a [NavController].
 *
 * `NavigationScene` offers a [NavigationSceneScope] that allows for fine-grained control over
 * how and where individual navigation entries are rendered using [NavigationSceneScope.EntryContent].
 * This is particularly useful for implementing custom layouts (e.g., side-by-side panes, lists with details) or
 * complex animated transitions that are not covered by the standard [Navigation] composable.
 *
 * Example usage:
 * ```
 * NavigationScene(navController, destinations) { // this: NavigationSceneScope
 *     val currentEntry by navController.currentNavEntryAsState()
 *     AnimatedContent(
 *         targetState = currentEntry,
 *         contentKey = { it?.contentKey() },
 *         transitionSpec = { navigationFadeInOut() }
 *     ) {
 *         EntryContent(it) // Renders the content of the current navigation entry
 *     }
 * }
 * ```
 *
 * @param navController The NavController to use for navigation
 * @param destinationResolver A function that resolves a destination by name (used during restoration from saved state)
 * @param handleSystemBackEvent Whether to handle system back events (default: true)
 * @param scene Scene builder composable function that defines how navigation entries are rendered
 */
@Composable
@Suppress("CognitiveComplexMethod")
public fun NavigationScene(
    navController: NavController,
    destinationResolver: (name: String) -> NavDestination<*>?,
    handleSystemBackEvent: Boolean = true,
    scene: @Composable NavigationSceneScope.() -> Unit
) {
    // resolve destinations in advance
    LaunchedEffect(navController) {
        navController.resolveNavDestinations(destinationResolver)
    }
    // back handler
    if (handleSystemBackEvent) {
        val canNavigateBack by navController.canNavigateBackAsState()
        BackHandler(canNavigateBack, navController::back)
    }
    val visibleEntries = remember { mutableSetOf<NavEntry<*>>() }
    // display current entry + animate enter/exit
    CompositionLocalProvider(LocalNavController provides navController) {
        val navScope = remember {
            NavigationSceneScope { entry ->
                if (!entry.isResolved) entry.resolveDestination(destinationResolver)
                NavEntryContent(entry)
                DisposableEffect(entry) {
                    if (visibleEntries.contains(entry))
                        error("The same entry (${entry.destination.name}) should not be displayed twice")
                    visibleEntries.add(entry)
                    onDispose {
                        visibleEntries.remove(entry)
                    }
                }
            }
        }
        navScope.scene()
    }
}