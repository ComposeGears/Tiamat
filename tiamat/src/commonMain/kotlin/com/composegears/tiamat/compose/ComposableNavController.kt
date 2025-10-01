package com.composegears.tiamat.compose

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavDestination
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import com.composegears.tiamat.navigation.NavEntry
import com.composegears.tiamat.navigation.SavedState

/**
 * CompositionLocal that provides access to the current NavController.
 */
internal val LocalNavController = staticCompositionLocalOf<NavController?> { null }

// ------------- NavController: general --------------------------------------------------------------------------------

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

// ------------- NavController: utils  ---------------------------------------------------------------------------------

/**
 * Collects values from this [NavController.navStateFlow] and represents its latest value via State.
 *
 * @return A State containing the current NavState
 */
@Composable
public fun NavController.navStateAsState(): State<NavController.NavState> =
    navStateFlow.collectAsState()

/**
 * Collects values from this [NavController.navStateFlow] and represents its latest value via State.
 *
 * @return A State containing the current navigation stack
 */
@Composable
public fun NavController.navStackAsState(): State<List<NavEntry<*>>> {
    val navState by navStateFlow.collectAsState()
    return remember { derivedStateOf { navState.stack } }
}

/**
 * Collects values from this [NavController.navStateFlow] and represents its latest value via State.
 *
 * @return A State containing a boolean value that is true when back navigation is possible
 */
@Composable
public fun NavController.canNavigateBackAsState(): State<Boolean> {
    val navState by navStateFlow.collectAsState()
    return remember { derivedStateOf { navState.stack.size > 1 } }
}

/**
 * Collects values from [NavController.navStateFlow], extracts the targetEntry
 * from the current transition, and represents its latest value via State.
 *
 * @return A State containing the current NavEntry, or null if there isn't one
 */
@Composable
public fun NavController.currentNavEntryAsState(): State<NavEntry<*>?> {
    val navState by navStateFlow.collectAsState()
    return remember { derivedStateOf { navState.stack.lastOrNull() } }
}

/**
 * Collects values from [NavController.navStateFlow], extracts the targetDestination
 * from the current transition, and represents its latest value via State.
 *
 * @return A State containing the current NavDestination, or null if there isn't one
 */
@Composable
public fun NavController.currentNavDestinationAsState(): State<NavDestination<*>?> {
    val navState by navStateFlow.collectAsState()
    return remember { derivedStateOf { navState.stack.lastOrNull()?.destination } }
}