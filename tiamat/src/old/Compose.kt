package com.composegears.tiamat

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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.input.pointer.pointerInput
import com.composegears.tiamat.TransitionController.Event.*
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch

internal val LocalNavController = staticCompositionLocalOf<NavController?> { null }
internal val LocalNavEntry = staticCompositionLocalOf<NavEntry<*>?> { null }

public enum class StorageMode {
    /**
     * Savable storage, persist internal cleanups
     */
    SavedState,

    /**
     * In memory data storage, NavController will reset on data loss
     */
    Memory
}

/**
 * Remembers a `NavController`.
 *
 * @param key The key for the NavController.
 * @param storageMode The storage mode for the NavController.
 * @param startDestination The start destination for the NavController.
 * @param destinations The array of allowed destinations for this NavController.
 * @param configuration The action to be called after NavController created/restored.
 * @return The remembered NavController.
 */
@Composable
@Suppress("ComposableParamOrder")
public fun rememberNavController(
    key: String? = null,
    storageMode: StorageMode? = null,
    startDestination: NavDestination<*>? = null,
    destinations: Array<NavDestination<*>>,
    configuration: NavController.() -> Unit = {}
): NavController = rememberNavController(
    key = key,
    storageMode = storageMode,
    startDestination = startDestination?.toNavEntry(),
    destinations = destinations,
    configuration = configuration
)

/**
 * Remembers a `NavController`.
 *
 * @param key The key for the NavController.
 * @param storageMode The storage mode for the NavController.
 * @param startDestination The start destination for the NavController.
 * @param startDestinationNavArgs The navigation navArgs for the start destination.
 * @param startDestinationFreeArgs The navigation freeArgs for the start destination.
 * @param destinations The array of allowed destinations for this NavController.
 * @param configuration The action to be called after NavController created/restored.
 * @return The remembered NavController.
 */
@Composable
@Suppress("ComposableParamOrder")
public fun <T> rememberNavController(
    key: String? = null,
    storageMode: StorageMode? = null,
    startDestination: NavDestination<T>,
    startDestinationNavArgs: T? = null,
    startDestinationFreeArgs: Any? = null,
    destinations: Array<NavDestination<*>>,
    configuration: NavController.() -> Unit = {}
): NavController = rememberNavController(
    key = key,
    storageMode = storageMode,
    startDestination = startDestination.toNavEntry(
        navArgs = startDestinationNavArgs,
        freeArgs = startDestinationFreeArgs
    ),
    destinations = destinations,
    configuration = configuration
)

/**
 * Remembers a `NavController`.
 *
 * @param key The key for the NavController.
 * @param storageMode The storage mode for the NavController.
 * @param startDestination The start destination for the NavController.
 * @param destinations The array of allowed destinations for this NavController.
 * @param configuration The action to be called after NavController created/restored.
 * @return The remembered NavController.
 */
@Composable
@Suppress("ComposableParamOrder")
public fun <T> rememberNavController(
    key: String? = null,
    storageMode: StorageMode? = null,
    startDestination: NavEntry<T>?,
    destinations: Array<NavDestination<*>>,
    configuration: NavController.() -> Unit = {}
): NavController {
    val parent = LocalNavController.current
    val parentNavEntry = LocalNavEntry.current
    val navControllersStorage = parentNavEntry?.navControllersStorage ?: rootNavControllersStore()
    val finalStorageMode = storageMode ?: parent?.storageMode ?: StorageMode.Memory

    // attach to system save logic and perform model save on it
    if (parent == null) rememberSaveable(
        saver = Saver(
            save = { navControllersStorage.saveToSaveState() },
            restore = { navControllersStorage.restoreFromSavedState(it) }
        ),
        init = { }
    )
    // create/restore nav controller from storage
    val parentRegistry = LocalSaveableStateRegistry.current
    val navController = remember {
        navControllersStorage
            .restoreOrCreate(
                key = key,
                parent = parent,
                storageMode = finalStorageMode,
                canBeSaved = parentRegistry
                    ?.takeIf { storageMode == StorageMode.SavedState }
                    ?.let { it::canBeSaved }
                    ?: { true },
                startDestination = startDestination,
                destinations = destinations
            )
            .apply(configuration)
            .apply { followParentsRoute() }
    }
    // attach/detach to parent storage
    DisposableEffect(navController) {
        navControllersStorage.attachNavController(navController)
        onDispose {
            navControllersStorage.detachNavController(navController)
            // we should close navController in case it is not `saved` to be restored later
            if (!navControllersStorage.isSaved(navController))
                navController.close()
        }
    }
    return navController
}

@Composable
@Suppress("CognitiveComplexMethod")
private fun <Args> AnimatedVisibilityScope.EntryContent(
    entry: NavEntry<Args>
) {
    Box {
        val navController = LocalNavController.current ?: error("NavController is not attached")
        // gen save state
        val saveRegistry = remember(entry) {
            val registry = SaveableStateRegistry(entry.savedState, navController.canBeSaved)
            entry.savedStateSaver = registry::performSave
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
                entry.destination.extensions.onEach {
                    if (it is ContentExtension && it.getType() == ContentExtension.Type.Underlay) with(it) {
                        Content()
                    }
                }
                // destination content
                with(entry.destination) {
                    Content()
                }
                // extensions after-content
                entry.destination.extensions.onEach {
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
            // invalidate navController routing state
            navController.invalidateRoute()
            // save state handle
            onDispose {
                entry.savedStateSaver = null
                // entry goes into backstack, save active subNavController
                if (entry in navController.getBackStack() || entry == navController.currentNavEntry) {
                    entry.saveState(saveRegistry.performSave())
                } else {
                    entry.close()
                }
            }
        }
    }
}

/**
 * Displays a content of `NavController` (see [rememberNavController])
 *
 * @param navController The NavController to use.
 * @param modifier The modifier to apply to the content.
 * @param handleSystemBackEvent Whether to handle the system back event.
 * @param contentTransformProvider The provider for the content transform.
 *
 * @see [NavController.navigate]
 * @see [NavController.replace]]
 * @see [NavController.back]
 * @see [navController]
 * @see [navArgs]
 * @see [navResult]
 * @see [rememberViewModel]
 */
@Composable
@Suppress("CognitiveComplexMethod")
public fun Navigation(
    navController: NavController,
    modifier: Modifier = Modifier,
    handleSystemBackEvent: Boolean = true,
    contentTransformProvider: (isForward: Boolean) -> ContentTransform = { navigationFadeInOut() }
) {
    if (handleSystemBackEvent) BackHandler(navController.canGoBack, navController::back)
    // display current entry + animate enter/exit
    CompositionLocalProvider(LocalNavController provides navController) {
        // seekable transition has a bug when one of props is `null`, so we will use stub destination instead of `null`
        val stubEntry = remember { NavEntry(NavDestinationImpl<Unit>("Stub", emptyList()) {}) }
        val transitionState = remember {
            SeekableTransitionState<NavEntry<*>>(navController.currentNavEntry ?: stubEntry)
        }
        // state controller
        LaunchedEffect(navController.currentNavEntry) {
            val targetValue = navController.currentNavEntry ?: stubEntry
            val controller = navController.transitionController
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
            contentKey = { (it as? NavEntry<*>)?.let { d -> "${d.destination.name}:${d.navId}" }.orEmpty() },
            contentAlignment = Alignment.Center,
            modifier = modifier,
            transitionSpec = {
                val transform = when {
                    navController.isInitialTransition -> ContentTransform(
                        targetContentEnter = EnterTransition.None,
                        initialContentExit = ExitTransition.None,
                        sizeTransform = null
                    )
                    navController.contentTransition != null -> navController.contentTransition!!
                    else -> contentTransformProvider(navController.isForwardTransition)
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


// ------------------ internal utils -------------------------

@Suppress("UNCHECKED_CAST")
internal fun <Model : TiamatViewModel> MutableMap<String, TiamatViewModel>.getModel(
    key: String,
    provider: () -> Model
): Model {
    val storeKey = "Model#$key"
    return getOrPut(storeKey, provider) as Model
}