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
import androidx.compose.ui.Modifier
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
    if (handleSystemBackEvent) NavBackHandler(navController.canGoBack, navController::back)
    // display current entry + animate enter/exit
    CompositionLocalProvider(LocalNavController provides navController) {
        val stubEntry = remember { NavEntry(NavDestination.Stub) }
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
                    targetContentZIndex = contentZIndex,
                    sizeTransform = transform.sizeTransform
                )
            },
        ) {
            if (it != stubEntry) EntryContent(it)
        }
    }
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
 * Gets the extension of the specified type from the current [NavDestination].
 *
 * @param P The type of the extension.
 * @return The extension of the specified type, or null if not found.
 */
@Composable
public inline fun <reified P : Extension<*>> NavDestinationScope<*>.ext(): P? =
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
    navEntry.viewModels.getModel(key, provider)
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
        restore = { navEntry.viewModels.getModel(key) { provider(it) } }
    ),
    init = { navEntry.viewModels.getModel(key) { provider(null) } }
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
    navController.sharedViewModels.getModel(key, provider)
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