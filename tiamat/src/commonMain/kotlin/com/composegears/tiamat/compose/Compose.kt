package com.composegears.tiamat.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composegears.tiamat.compose.TransitionController.Event.*
import com.composegears.tiamat.navigation.*
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf

// ------------- Local Providers ---------------------------------------------------------------------------------------

/**
 * CompositionLocal that provides current [AnimatedVisibilityScope].
 */
public val LocalNavAnimatedVisibilityScope: ProvidableCompositionLocal<AnimatedVisibilityScope?> =
    staticCompositionLocalOf { null }

/**
 * CompositionLocal that provides access to the current NavController.
 */
internal val LocalNavController = staticCompositionLocalOf<NavController?> { null }

/**
 * CompositionLocal that provides access to the current NavEntry.
 */
internal val LocalNavEntry = staticCompositionLocalOf<NavEntry<*>?> { null }

// ------------- NavController -----------------------------------------------------------------------------------------

/**
 * Creates and remembers a NavController without a start destination.
 *
 * @param key Optional identifier for this NavController
 * @param saveable Whether the NavController's state should be saved and restored (defaults to the parent's value or true)
 * @param savedState Optional saved state to restore from
 * @param configuration Additional configuration actions to apply to the NavController
 * @return A remembered NavController instance
 */
@Composable
public fun rememberNavController(
    key: String? = null,
    saveable: Boolean? = null,
    savedState: SavedState? = null,
    configuration: NavController.() -> Unit = {}
): NavController = rememberNavController(
    key = key,
    saveable = saveable,
    startEntry = null,
    savedState = savedState,
    configuration = configuration,
)

/**
 * Creates and remembers a NavController.
 *
 * @param key Optional identifier for this NavController
 * @param saveable Whether the NavController's state should be saved and restored (defaults to the parent's value or true)
 * @param savedState Optional saved state to restore from
 * @param startDestination The initial destination to navigate to
 * @param configuration Additional configuration actions to apply to the NavController
 * @return A remembered NavController instance
 */
@Composable
public fun rememberNavController(
    key: String? = null,
    saveable: Boolean? = null,
    savedState: SavedState? = null,
    startDestination: NavDestination<*>? = null,
    configuration: NavController.() -> Unit = {}
): NavController = rememberNavController(
    key = key,
    saveable = saveable,
    startEntry = startDestination?.toNavEntry(),
    savedState = savedState,
    configuration = configuration,
)

/**
 * Creates and remembers a NavController.
 *
 * @param key Optional identifier for this NavController
 * @param saveable Whether the NavController's state should be saved and restored (defaults to the parent's value or true)
 * @param savedState Optional saved state to restore from
 * @param startEntry The initial entry to navigate to
 * @param configuration Additional configuration actions to apply to the NavController
 * @return A remembered NavController instance
 */
@Composable
@Suppress("CyclomaticComplexMethod", "CognitiveComplexMethod")
public fun rememberNavController(
    key: String? = null,
    saveable: Boolean? = null,
    savedState: SavedState? = null,
    startEntry: NavEntry<*>? = null,
    configuration: NavController.() -> Unit = {}
): NavController {
    val parent = LocalNavController.current
    val parentNavEntry = LocalNavEntry.current
    val navControllersStorage = parentNavEntry?.navControllerStore
    val isSaveable = saveable ?: parent?.saveable ?: true

    fun createNavController() =
        if (savedState != null) NavController.restoreFromSavedState(parent, savedState)
        else NavController.create(key, isSaveable, parent, startEntry, configuration)

    val navController =
        if (isSaveable && navControllersStorage == null) rememberSaveable(
            key = key,
            saver = Saver(
                save = { it.saveToSavedState() },
                restore = { NavController.restoreFromSavedState(parent, it) }
            ),
            init = { createNavController() }
        ) else remember {
            if (navControllersStorage != null) {
                var navController = navControllersStorage.get(key)
                if (navController == null) {
                    navController = createNavController()
                    if (isSaveable) navControllersStorage.add(navController)
                }
                navController
            } else createNavController()
        }

    DisposableEffect(navController) {
        onDispose {
            // Dispose called in 2 cases
            // 1. NavEntry is closed due to navigation (detached from UI, attached to NC)
            // 2. `rememberNavController` composable leave entry composition (eg: switch between 2 Nav-s)
            val shouldClear = when {
                !isSaveable -> true // not saveable -> clear
                navControllersStorage == null -> true // no storage + dispose means root NC leave composition -> clear
                parentNavEntry.isAttachedToUI -> true // NC leave entry composition till entry on screen -> clear
                else -> false
            }
            if (shouldClear) {
                navControllersStorage?.remove(navController)
                navController.close()
            }
        }
    }
    return navController
}

// ------------- Navigation & nav-content ------------------------------------------------------------------------------

@Composable
@Suppress("CognitiveComplexMethod", "UNCHECKED_CAST")
private fun <Args : Any> NavEntryContent(
    entry: NavEntry<Args>
) {
    val destination = entry.destination
    if (destination is ComposeNavDestination<Args>) Box {
        val entrySaveableStateRegistry = rememberEntrySaveableStateRegistry(entry)
        val entryContentLifecycleOwner = rememberEntryContentLifecycleOwner(entry)
        // display content
        CompositionLocalProvider(
            LocalSaveableStateRegistry provides entrySaveableStateRegistry,
            LocalLifecycleOwner provides entryContentLifecycleOwner,
            LocalViewModelStoreOwner provides entry,
            LocalNavEntry provides entry,
        ) {
            val scope = remember(entry) { NavDestinationScopeImpl(entry) }
            // entry content
            with(scope) {
                // extensions before-content
                destination.extensions.onEach {
                    if (it is ContentExtension && it.getType() == ContentExtension.Type.Underlay) with(it) {
                        Content()
                    }
                }
                // destination content
                with(destination) {
                    Content()
                }
                // extensions after-content
                destination.extensions.onEach {
                    if (it is ContentExtension && it.getType() == ContentExtension.Type.Overlay) with(it) {
                        Content()
                    }
                }
            }
        }
        // save state when `this entry`/`parent entry` goes into backStack
        DisposableEffect(entry) {
            entry.attachToUI()
            entry.setSavedStateSaver(entrySaveableStateRegistry::performSave)
            // save state handle
            onDispose {
                entry.setSavedStateSaver(null)
                entryContentLifecycleOwner.close()
                if (entry.isAttachedToNavController) entry.savedState = entrySaveableStateRegistry.performSave()
                entry.detachFromUI()
            }
        }
    }
}

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
                NavDestinationImpl(
                    name = "Stub",
                    argsType = typeOf<Unit>(),
                    extensions = emptyList(),
                    content = {}
                )
            )
        }
        val state by navController.currentTransitionFlow.collectAsState()
        // seekable transition has a bug when one of props is `null`, so we will use stub destination instead of `null`
        val targetValue = remember(state) { state?.targetEntry ?: stubEntry }
        val transitionData = remember(state) { state?.transitionData as? TransitionData }
        val transitionState = remember { SeekableTransitionState<NavEntry<*>>(stubEntry) }
        // state/transition controller
        LaunchedEffect(state) {
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
                            is Finish -> transitionState.animateTo(targetValue, item.animationSpec)
                            is Update -> transitionState.seekTo(item.value, targetValue)
                        }
                    }
            } else transitionState.animateTo(targetValue)
        }
        // content
        val transition = rememberTransition(transitionState)
        var contentZIndex by remember { mutableFloatStateOf(0f) }
        transition.AnimatedContent(
            contentKey = { it.contentKey() },
            contentAlignment = Alignment.Center,
            modifier = modifier,
            transitionSpec = {
                val transform = when {
                    transition.targetState == stubEntry || transition.currentState == stubEntry -> ContentTransform(
                        targetContentEnter = EnterTransition.None,
                        initialContentExit = ExitTransition.None,
                        sizeTransform = null
                    )
                    transitionData?.contentTransform != null -> transitionData.contentTransform
                    else -> contentTransformProvider(state?.isForward ?: true)
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
                    if (it != targetValue && transition.isRunning) Box(
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
    if (handleSystemBackEvent) {
        val hasBackEntries by navController.hasBackEntriesAsState()
        BackHandler(hasBackEntries, navController::back)
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

// ------------- Nav controller extras ---------------------------------------------------------------------------------

/**
 * Collects values from this [NavController.currentBackStackFlow] and represents its latest value via State.
 *
 * @return A State containing a boolean value that is true when back navigation is possible
 */
@Composable
public fun NavController.hasBackEntriesAsState(): State<Boolean> {
    val backstack by currentBackStackFlow.collectAsState()
    return remember { derivedStateOf { backstack.isNotEmpty() } }
}

/**
 * Collects values from [NavController.currentTransitionFlow], extracts the targetEntry
 * from the current transition, and represents its latest value via State.
 *
 * @return A State containing the current NavEntry, or null if there isn't one
 */
@Composable
public fun NavController.currentNavEntryAsState(): State<NavEntry<*>?> {
    val state by currentTransitionFlow.collectAsState()
    return remember { derivedStateOf { state?.targetEntry } }
}

/**
 * Collects values from [NavController.currentTransitionFlow], extracts the targetDestination
 * from the current transition, and represents its latest value via State.
 *
 * @return A State containing the current NavDestination, or null if there isn't one
 */
@Composable
public fun NavController.currentNavDestinationAsState(): State<NavDestination<*>?> {
    val state by currentTransitionFlow.collectAsState()
    return remember(state) { derivedStateOf { state?.targetEntry?.destination } }
}

// ------------- NavDestinationScope extras ----------------------------------------------------------------------------
// ------------- NavDestinationScope extras : general ------------------------------------------------------------------

/**
 * @return The current [NavController].
 */
@Composable
@Suppress("UnusedReceiverParameter")
public fun NavDestinationScope<*>.navController(): NavController =
    LocalNavController.current ?: error("not attached to navController")

/**
 * @return The current [NavEntry].
 */
@Composable
public fun NavDestinationScope<*>.navEntry(): NavEntry<*> = navEntry

/**
 * Gets the extension of the specified type from the current [ComposeNavDestination].
 *
 * @param P The type of the extension.
 * @return The extension of the specified type, or null if not found.
 */
@Composable
public inline fun <reified P : NavExtension<*>> NavDestinationScope<*>.ext(): P? =
    navEntry().destination.ext<P>()

// ------------- NavDestinationScope extras : navArgs ------------------------------------------------------------------

/**
 * Gets the navigation arguments from the current [NavEntry] or throw an exception.
 *
 * @param Args The type of the navigation arguments.
 * @return The navigation arguments.
 */
@Composable
public fun <Args : Any> NavDestinationScope<Args>.navArgs(): Args =
    navArgsOrNull() ?: error("args not provided or null, consider use navArgsOrNull()")

/**
 * Gets the navigation arguments from the current [NavEntry], or null if not provided.
 *
 * @param Args The type of the navigation arguments.
 * @return The navigation arguments, or null if not provided.
 */
@Suppress("CastToNullableType")
@Composable
public fun <Args : Any> NavDestinationScope<Args>.navArgsOrNull(): Args? = remember {
    navEntry.getNavArgs()
}

/**
 * Clears the navigation arguments from the [NavEntry].
 */
public fun NavDestinationScope<*>.clearNavArgs() {
    navEntry.clearNavArgs()
}

// ------------- NavDestinationScope extras : freeArgs -----------------------------------------------------------------

/**
 * Gets the free arguments from the current [NavEntry].
 *
 * @param T The type of the free arguments to retrieve.
 * @return The free arguments of the specified type,or null if not present or not of type [T].
 */
@Composable
public inline fun <reified T> NavDestinationScope<*>.freeArgs(): T? = remember {
    navEntry.getFreeArgs()
}

/**
 * Clears the free arguments from the [NavEntry].
 */
public fun NavDestinationScope<*>.clearFreeArgs() {
    navEntry.clearFreeArgs()
}

// ------------- NavDestinationScope extras : navResult ----------------------------------------------------------------

/**
 * Gets the navigation result from the current [NavEntry].
 *
 * @param T The type of the navigation result to retrieve.
 * @return The navigation result of the specified type, or null if not present or not of type [T].
 */
@Composable
public inline fun <reified T> NavDestinationScope<*>.navResult(): T? = remember {
    navEntry.getNavResult()
}

/**
 * Clears the navigation result from the [NavEntry].
 */
public fun NavDestinationScope<*>.clearNavResult() {
    navEntry.clearNavResult()
}

// ------------- NavDestinationScope viewModels ------------------------------------------------------------------------

@Composable
public inline fun <reified VM : ViewModel> saveableViewModel(
    viewModelStoreOwner: ViewModelStoreOwner =
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
    key: String? = null,
    noinline initializer: CreationExtras.(savedState: MutableSavedState) -> VM
): VM {
    val viewModelSavedState = rememberSaveable { MutableSavedState() }
    return viewModel<VM>(
        viewModelStoreOwner = viewModelStoreOwner,
        key = key,
        initializer = { initializer(viewModelSavedState) }
    )
}