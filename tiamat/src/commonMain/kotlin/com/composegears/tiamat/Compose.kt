package com.composegears.tiamat

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

internal val LocalNavController = staticCompositionLocalOf<NavController?> { null }
internal val LocalNavEntry = staticCompositionLocalOf<NavEntry<*>?> { null }

enum class StorageMode {
    /**
     * Savable storage, persist internal cleanups
     */
    Savable,

    /**
     * In memory data storage, navController will reset on data loss
     */
    ResetOnDataLoss
}

/**
 * Create and provide [NavController] instance to be used in [Navigation]
 *
 * @param key optional key, used to identify NavController's (eg: nc.parent.key == ...)
 * @param storageMode data storage mode, default is parent mode or if it is root [StorageMode.ResetOnDataLoss]
 * @param startDestination destination to be used as initial
 * @param destinations array of allowed destinations for this controller
 * @param configuration an action to be called after [NavController] created/restored
 */
@Composable
@Suppress("ComposableParamOrder")
fun rememberNavController(
    key: String? = null,
    storageMode: StorageMode? = null,
    startDestination: NavDestination<*>? = null,
    destinations: Array<NavDestination<*>>,
    configuration: NavController.() -> Unit = {}
) = rememberNavController(
    key = key,
    storageMode = storageMode,
    startDestination = startDestination?.toNavEntry(),
    destinations = destinations,
    configuration = configuration
)

/**
 * Create and provide [NavController] instance to be used in [Navigation]
 *
 * @param key optional key, used to identify NavController's (eg: nc.parent.key == ...)
 * @param storageMode data storage mode, default is parent mode or if it is root [StorageMode.ResetOnDataLoss]
 * @param startDestination destination to be used as initial
 * @param startDestinationNavArgs initial destination navArgs
 * @param startDestinationFreeArgs initial destination freeArgs
 * @param destinations array of allowed destinations for this controller
 * @param configuration an action to be called after [NavController] created/restored
 */
@Composable
@Suppress("ComposableParamOrder")
fun <T> rememberNavController(
    key: String? = null,
    storageMode: StorageMode? = null,
    startDestination: NavDestination<T>?,
    startDestinationNavArgs: T? = null,
    startDestinationFreeArgs: Any? = null,
    destinations: Array<NavDestination<*>>,
    configuration: NavController.() -> Unit = {}
) = rememberNavController(
    key = key,
    storageMode = storageMode,
    startDestination = startDestination?.toNavEntry(
        navArgs = startDestinationNavArgs,
        freeArgs = startDestinationFreeArgs
    ),
    destinations = destinations,
    configuration = configuration
)

/**
 * Create and provide [NavController] instance to be used in [Navigation]
 *
 * @param key optional key, used to identify NavController's (eg: nc.parent.key == ...)
 * @param storageMode data storage mode, default is parent mode or if it is root [StorageMode.ResetOnDataLoss]
 * @param startDestination destination entry (destination + args) to be used as initial
 * @param destinations array of allowed destinations for this controller
 * @param configuration an action to be called after [NavController] created/restored
 */
@Composable
@Suppress("ComposableParamOrder")
fun <T> rememberNavController(
    key: String? = null,
    storageMode: StorageMode? = null,
    startDestination: NavEntry<T>?,
    destinations: Array<NavDestination<*>>,
    configuration: NavController.() -> Unit = {}
): NavController {
    val parent = LocalNavController.current
    val parentNavEntry = LocalNavEntry.current
    val navControllersStorage = parentNavEntry?.navControllersStorage ?: rootNavControllersStore()
    val finalStorageMode = storageMode ?: parent?.storageMode ?: StorageMode.ResetOnDataLoss

    // attach to system save logic and perform model save on it
    if (parent == null) rememberSaveable(
        saver = Saver(
            save = { navControllersStorage.saveToSaveState() },
            restore = {
                navControllersStorage.restoreFromSavedState(it)
                0
            }
        ),
        init = { 0 }
    )
    // create/restore nav controller from storage
    val navController = remember {
        navControllersStorage.restoreOrCreate(
            key = key,
            parent = parent,
            storageMode = finalStorageMode,
            startDestination = startDestination,
            destinations = destinations
        ).apply(configuration)
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
private fun <Args> AnimatedVisibilityScope.DestinationContent(entry: NavEntry<Args>) {
    val scope = remember(entry) { NavDestinationScopeImpl(entry, this) }
    with(entry.destination) {
        scope.PlatformContentWrapper {
            Content()
        }
    }
}

@Composable
private fun BoxScope.Overlay() {
    Box(
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
}

/**
 * Created a content of [NavController] (see [rememberNavController])
 *
 * Root element of the content is [AnimatedContent]
 *
 * @param navController displayed navController
 * @param modifier modifier to be passed to root [AnimatedContent]
 * @param handleSystemBackEvent allow to call [NavController.back] on system `back` event if possible
 * @param contentTransformProvider default nav transition provided to be used
 * if no transition provided by navigation functions
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
@OptIn(ExperimentalAnimationApi::class)
fun Navigation(
    navController: NavController,
    modifier: Modifier = Modifier,
    handleSystemBackEvent: Boolean = true,
    contentTransformProvider: (isForward: Boolean) -> ContentTransform = { navigationFadeInOut() }
) {
    if (handleSystemBackEvent) NavBackHandler(navController.canGoBack, navController::back)
    // display current entry + animate enter/exit
    AnimatedContent(
        targetState = navController.currentNavEntry,
        contentKey = { it?.let { "${it.destination.name}:${it.navId}" }.orEmpty() },
        contentAlignment = Alignment.Center,
        modifier = modifier,
        transitionSpec = {
            when {
                navController.isInitialTransition -> ContentTransform(
                    targetContentEnter = EnterTransition.None,
                    initialContentExit = ExitTransition.None,
                    sizeTransform = null
                )
                navController.contentTransition != null -> navController.contentTransition!!
                else -> contentTransformProvider(navController.isForwardTransition)
            }
        },
        label = "nav_controller_${navController.key ?: "no_key"}",
    ) {
        if (it != null) Box {
            // gen save state
            val saveRegistry = remember(it) {
                val registry = SaveableStateRegistry(it.savedState) { true }
                it.savedStateSaver = registry::performSave
                registry
            }
            // display content
            CompositionLocalProvider(
                LocalSaveableStateRegistry provides saveRegistry,
                LocalNavController provides navController,
                LocalNavEntry provides it,
            ) {
                DestinationContent(it)
            }
            // prevent clicks during transition animation
            if (transition.isRunning) Overlay()
            // save state when `this entry`/`parent entry` goes into backStack
            DisposableEffect(it) {
                onDispose {
                    it.savedStateSaver = null
                    // entry goes into backstack, store active subNavController
                    if (it in navController.getBackStack() || it == navController.currentNavEntry) {
                        it.saveState(saveRegistry.performSave())
                    } else {
                        it.close()
                    }
                }
            }
        }
    }
}

/**
 * Provides current [NavController] instance
 */
@Composable
@Suppress("UnusedReceiverParameter")
fun NavDestinationScope<*>.navController(): NavController =
    LocalNavController.current ?: error("not attached to navController")

/**
 * Provides nav arguments passed into navigate forward function for current destination
 *
 * @see [NavController.navigate]
 * @see [NavController.replace]
 *
 * @return navigation arguments provided to [NavController.navigate] function or exception
 */
@Composable
@Suppress("CastToNullableType")
fun <Args> NavDestinationScope<Args>.navArgs(): Args = remember {
    navEntry.navArgs ?: error("args not provided or null, consider use navArgsOrNull()")
}

/**
 * Provides nav arguments passed into navigate forward function for current destination
 *
 * @see [NavController.navigate]
 * @see [NavController.replace]
 *
 * @return navigation arguments provided to [NavController.navigate] function or null
 */
@Composable
@Suppress("CastToNullableType")
fun <Args> NavDestinationScope<Args>.navArgsOrNull(): Args? = remember {
    navEntry.navArgs
}

/**
 * Provides free nav arguments passed into navigate forward function for current destination
 *
 * @see [NavController.navigate]
 * @see [NavController.replace]
 *
 * @return free nav arguments provided to [NavController.navigate] function or null
 */
@Composable
@Suppress("UNCHECKED_CAST", "CastToNullableType")
fun <T> NavDestinationScope<*>.freeArgs(): T? = remember {
    navEntry.freeArgs as T?
}

/**
 * Clear provided free args
 */
fun NavDestinationScope<*>.clearFreeArgs() {
    navEntry.freeArgs = null
}

/**
 * Provides nav arguments provided as result into navigate back function for current destination
 *
 * @see [NavController.back]
 */
@Composable
@Suppress("UNCHECKED_CAST", "CastToNullableType")
fun <Result> NavDestinationScope<*>.navResult(): Result? = remember {
    navEntry.navResult as Result?
}

/**
 * Clear provided nav result
 */
fun NavDestinationScope<*>.clearNavResult() {
    navEntry.navResult = null
}

/**
 * Provide (create or restore) viewModel instance bound to navigation entry
 *
 * @param provider default viewModel instance provider
 */
@Composable
inline fun <reified Model : TiamatViewModel> NavDestinationScope<*>.rememberViewModel(
    noinline provider: () -> Model
): Model = rememberViewModel(className<Model>(), provider)

/**
 * Recommended to use `rememberViewModel(key, provider)` instead
 *
 * Provide (create or restore) viewModel instance bound to navigation entry
 *
 * @param key provides unique key part
 * @param provider default viewModel instance provider
 */
@Composable
@Suppress("UNCHECKED_CAST")
fun <Model : TiamatViewModel> NavDestinationScope<*>.rememberViewModel(
    key: String,
    provider: () -> Model
): Model = remember {
    val storeKey = "Model#$key"
    navEntry.viewModels.getOrPut(storeKey, provider) as Model
}

/**
 * Provide sharedViewModel instance to provided [NavController] (default is current)
 *
 * @param navController current navController to which the ViewModel will be attached
 * @param provider default viewModel instance provider
 */
@Composable
inline fun <reified Model : TiamatViewModel> NavDestinationScope<*>.rememberSharedViewModel(
    navController: NavController = navController(),
    noinline provider: () -> Model
): Model = rememberSharedViewModel(className<Model>(), navController, provider)

/**
 * Provide sharedViewModel instance to provided [NavController] (default is current)
 *
 * @param key provides unique key part
 * @param navController current navController to which the ViewModel will be attached
 * @param provider default viewModel instance provider
 */
@Composable
@Suppress("UNCHECKED_CAST")
fun <Model : TiamatViewModel> NavDestinationScope<*>.rememberSharedViewModel(
    key: String,
    navController: NavController = navController(),
    provider: () -> Model
): Model = remember {
    val storeKey = "Model#$key"
    navController.sharedViewModels.getOrPut(storeKey, provider) as Model
}