package com.composegears.tiamat.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.input.pointer.pointerInput
import com.composegears.tiamat.compose.TransitionController.Event.*
import com.composegears.tiamat.navigation.*
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch

internal val LocalNavController = staticCompositionLocalOf<NavController?> { null }
internal val LocalNavEntry = staticCompositionLocalOf<NavEntry<*>?> { null }

@Composable
public fun rememberNavController(
    key: String? = null,
    saveable: Boolean? = null,
    startDestination: NavDestination<*>? = null,
    savedState: SavedState? = null,
    configuration: NavController.() -> Unit = {}
): NavController = rememberNavController(
    key = key,
    saveable = saveable,
    startEntry = startDestination?.toNavEntry(),
    savedState = savedState,
    configuration = configuration,
)

@Composable
@Suppress("CyclomaticComplexMethod", "CognitiveComplexMethod")
public fun rememberNavController(
    key: String? = null,
    saveable: Boolean? = null,
    startEntry: NavEntry<*>? = null,
    savedState: SavedState? = null,
    configuration: NavController.() -> Unit = {}
): NavController {
    val parent = LocalNavController.current
    val parentNavEntry = LocalNavEntry.current
    val navControllersStorage = parentNavEntry?.navControllersStorage
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

@Composable
@Suppress("CognitiveComplexMethod", "UNCHECKED_CAST")
private fun <Args> AnimatedVisibilityScope.EntryContent(
    entry: NavEntry<Args>
) {
    val destination = entry.destination
    if (destination is ComposeNavDestination<Args>) Box {
        val parentRegistry = LocalSaveableStateRegistry.current
        // gen save state
        val saveRegistry = remember(entry) {
            val registry = SaveableStateRegistry(
                restoredValues = entry.savedState as? Map<String, List<Any?>>?,
                canBeSaved = { parentRegistry?.canBeSaved(it) ?: true }
            )
            entry.setSavedStateSaver(registry::performSave)
            registry
        }
        // display content
        CompositionLocalProvider(
            LocalSaveableStateRegistry provides saveRegistry,
            LocalNavEntry provides entry,
        ) {
            val scope = remember(entry) { NavDestinationScopeImpl(entry, this@EntryContent) }
            // entry content
            scope.PlatformContentWrapper {
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
        // prevent clicks during transition animation
        if (transition.isRunning) Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach {
                                it.consume()
                            }
                        }
                    }
                }
        )
        // save state when `this entry`/`parent entry` goes into backStack
        DisposableEffect(entry) {
            entry.attachToUI()
            // save state handle
            onDispose {
                entry.setSavedStateSaver(null)
                if (entry.isAttachedToNavController) entry.savedState = saveRegistry.performSave()
                entry.detachFromUI()
            }
        }
    }
}

@Composable
@Suppress("CognitiveComplexMethod")
public fun Navigation(
    navController: NavController,
    destinations: Array<NavDestination<*>>,
    modifier: Modifier = Modifier,
    handleSystemBackEvent: Boolean = true,
    contentTransformProvider: (isForward: Boolean) -> ContentTransform = { navigationFadeInOut() },
) {
    if (handleSystemBackEvent) {
        val canGoGoBack by navController.canGoBackAsState()
        BackHandler(canGoGoBack, navController::back)
    }
    // display current entry + animate enter/exit
    CompositionLocalProvider(LocalNavController provides navController) {
        val stubEntry = remember { NavEntry(NavDestinationImpl<Unit>("Stub", emptyList()) {}) }
        val state by navController.currentTransitionFlow.collectAsState()
        // seekable transition has a bug when one of props is `null`, so we will use stub destination instead of `null`
        val targetValue = remember(state) { state?.targetEntry ?: stubEntry }
        val transitionData = remember(state) { state?.transitionData as? TransitionData }
        val transitionState = remember { SeekableTransitionState<NavEntry<*>>(stubEntry) }
        // state controller
        LaunchedEffect(state) {
            if (!targetValue.isResolved()) targetValue.resolveDestination(destinations)
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
            contentKey = { "${it.destination.name}:${it.uid}" },
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
            if (it != stubEntry) EntryContent(it)
        }
    }
}

@Composable
public fun NavController.canGoBackAsState(): State<Boolean> {
    val backstack by currentBackStackFlow.collectAsState()
    return remember(backstack) { derivedStateOf { backstack.isNotEmpty() } }
}

@Composable
public fun NavController.currentNavEntryAsState(): State<NavEntry<*>?> {
    val state by currentTransitionFlow.collectAsState()
    return remember(state) { derivedStateOf { state?.targetEntry } }
}

@Composable
public fun NavController.currentNavDestinationAsState(): State<NavDestination<*>?> {
    val state by currentTransitionFlow.collectAsState()
    return remember(state) { derivedStateOf { state?.targetEntry?.destination } }
}

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

/**
 * Gets the navigation arguments from the current [NavEntry] or throw an exception.
 *
 * @param Args The type of the navigation arguments.
 * @return The navigation arguments.
 */
@Composable
@Suppress("CastToNullableType")
public fun <Args> NavDestinationScope<Args>.navArgs(): Args = remember {
    navEntry.navArgs ?: error("args not provided or null, consider use navArgsOrNull()")
}

/**
 * Gets the navigation arguments from the current [NavEntry], or null if not provided.
 *
 * @param Args The type of the navigation arguments.
 * @return The navigation arguments, or null if not provided.
 */
@Composable
@Suppress("CastToNullableType")
public fun <Args> NavDestinationScope<Args>.navArgsOrNull(): Args? = remember {
    navEntry.navArgs
}

/**
 * Gets the free arguments from the current [NavEntry].
 *
 * @param T The type of the free arguments.
 * @return The free arguments, or null if not provided.
 */
@Suppress("UNCHECKED_CAST", "CastToNullableType")
public fun <T> NavDestinationScope<*>.freeArgs(): T? = navEntry.freeArgs as T?

/**
 * Clears the free arguments from the [NavEntry].
 */
public fun NavDestinationScope<*>.clearFreeArgs() {
    navEntry.freeArgs = null
}

/**
 * Gets the navigation result from the current [NavEntry].
 *
 * @param Result The type of the navigation result.
 * @return The navigation result, or null if not provided.
 */
@Suppress("UNCHECKED_CAST", "CastToNullableType")
public fun <Result> NavDestinationScope<*>.navResult(): Result? = navEntry.navResult as Result?

/**
 * Clears the navigation result from the [NavEntry].
 */
public fun NavDestinationScope<*>.clearNavResult() {
    navEntry.navResult = null
}

/**
 * Remembers a ViewModel in the [NavEntry].
 *
 * @param Model The type of the ViewModel.
 * @param provider The provider for the ViewModel.
 * @return The remembered ViewModel.
 */
@Composable
public inline fun <reified Model : TiamatViewModel> NavDestinationScope<*>.rememberViewModel(
    noinline provider: () -> Model
): Model = rememberViewModel(className<Model>(), provider)

/**
 * Remembers a ViewModel in the [NavEntry].
 *
 * @param Model The type of the ViewModel.
 * @param key The key for the ViewModel.
 * @param provider The provider for the ViewModel.
 * @return The remembered ViewModel.
 */
@Composable
public fun <Model : TiamatViewModel> NavDestinationScope<*>.rememberViewModel(
    key: String,
    provider: () -> Model
): Model = remember {
    navEntry.viewModelsStorage.get(key, provider)
}

/**
 * Remembers a saveable ViewModel in the [NavEntry].
 *
 * @param Model The type of the ViewModel.
 * @param provider The provider for the ViewModel.
 * @return The remembered saveable ViewModel.
 */
@Composable
public inline fun <reified Model> NavDestinationScope<*>.rememberSaveableViewModel(
    noinline provider: (SavedState?) -> Model
): Model where Model : TiamatViewModel, Model : Saveable =
    rememberSaveableViewModel(className<Model>(), provider)

/**
 * Remembers a saveable ViewModel in the [NavEntry].
 *
 * @param Model The type of the ViewModel.
 * @param key The key for the ViewModel.
 * @param provider The provider for the ViewModel.
 * @return The remembered saveable ViewModel.
 */
@Composable
public fun <Model> NavDestinationScope<*>.rememberSaveableViewModel(
    key: String,
    provider: (SavedState?) -> Model
): Model where Model : TiamatViewModel, Model : Saveable = rememberSaveable(
    saver = Saver(
        save = { it.saveToSaveState() },
        restore = { navEntry.viewModelsStorage.get(key) { provider(it) } }
    ),
    init = { navEntry.viewModelsStorage.get(key) { provider(null) } }
)

/**
 * Remembers a shared ViewModel in the [NavController].
 *
 * @param Model The type of the ViewModel.
 * @param navController The NavController to bind to.
 * @param provider The provider for the ViewModel.
 * @return The remembered shared ViewModel.
 */
@Composable
public inline fun <reified Model : TiamatViewModel> NavDestinationScope<*>.rememberSharedViewModel(
    navController: NavController = navController(),
    noinline provider: () -> Model
): Model = rememberSharedViewModel(className<Model>(), navController, provider)

/**
 * Remembers a shared ViewModel in the [NavController].
 *
 * @param Model The type of the ViewModel.
 * @param key The key for the ViewModel.
 * @param navController The NavController to bind to.
 * @param provider The provider for the ViewModel.
 * @return The remembered shared ViewModel.
 */
@Composable
public fun <Model : TiamatViewModel> NavDestinationScope<*>.rememberSharedViewModel(
    key: String,
    navController: NavController = navController(),
    provider: () -> Model
): Model = remember {
    navController.sharedViewModelsStorage.get(key, provider)
}