package com.composegears.tiamat

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Hold nav entry information
 */
@Stable
public class NavEntry<Args> private constructor(
    public val destination: NavDestination<Args>,
    navArgs: Args? = null,
    freeArgs: Any? = null,
    navResult: Any? = null,
    savedState: Map<String, List<Any?>>? = null,
    savedNavControllers: List<SavedState>? = null,
) : Route.Element {

    public companion object {
        private const val KEY_NAME = "name"
        private const val KEY_NAV_ID = "id"
        private const val KEY_NAV_ARGS = "navArgs"
        private const val KEY_FREE_ARGS = "freeArgs"
        private const val KEY_NAV_RESULT = "navResult"
        private const val KEY_SAVED_STATE = "savedState"
        private const val KEY_SAVED_NAV_CONTROLLERS = "savedNavControllers"

        internal fun restore(
            savedState: SavedState,
            destinations: Array<NavDestination<*>>,
        ): NavEntry<*> {
            val destination = (savedState[KEY_NAME] as String).let { name -> destinations.first { it.name == name } }
            return restore(savedState, destination)
        }

        @Suppress("UNCHECKED_CAST")
        private fun <Args> restore(
            savedState: SavedState,
            destination: NavDestination<Args>,
        ) = NavEntry(
            destination = destination,
            navArgs = savedState[KEY_NAV_ARGS] as Args,
            freeArgs = savedState[KEY_FREE_ARGS],
            navResult = savedState[KEY_NAV_RESULT],
            savedState = savedState[KEY_SAVED_STATE] as? Map<String, List<Any?>>?,
            savedNavControllers = savedState[KEY_SAVED_NAV_CONTROLLERS] as? List<SavedState>?
        ).also {
            it.navId = savedState[KEY_NAV_ID] as Long
        }
    }

    public var navArgs: Args? = navArgs
        internal set
    public var freeArgs: Any? by mutableStateOf(freeArgs)
        internal set
    public var navResult: Any? by mutableStateOf(navResult)
        internal set

    internal var navId: Long = -1
    internal val viewModels = mutableMapOf<String, TiamatViewModel>()
    internal var savedState: Map<String, List<Any?>> = savedState ?: emptyMap()
    internal var savedStateSaver: (() -> Map<String, List<Any?>>)? = null
    internal var canBeSaved: ((Any) -> Boolean)? = null
    internal val navControllersStorage = NavControllersStorage().apply { restoreFromSavedState(savedNavControllers) }

    public constructor(
        destination: NavDestination<Args>,
        navArgs: Args? = null,
        freeArgs: Any? = null,
        navResult: Any? = null
    ) : this(
        destination = destination,
        navArgs = navArgs,
        freeArgs = freeArgs,
        navResult = navResult,
        savedState = null,
    )

    internal constructor(
        navEntry: NavEntry<Args>
    ) : this(
        destination = navEntry.destination,
        navArgs = navEntry.navArgs,
        freeArgs = navEntry.freeArgs,
        navResult = navEntry.navResult,
        savedState = null,
    )

    internal fun saveState(saveState: Map<String, List<Any?>>) {
        this.savedState = saveState
        navControllersStorage.saveState()
    }

    private fun validateSaveable(data: Any?, type: String) {
        val canSave = data?.let { canBeSaved?.invoke(it) }
        if (canSave == false) error("The $type of ${destination.name} can't be saved")
    }

    internal fun saveToSaveState(): SavedState {
        validateSaveable(navArgs, "navArgs")
        validateSaveable(freeArgs, "freeArgs")
        validateSaveable(navResult, "navResult")
        savedStateSaver?.invoke()?.let { savedState = it }
        return mapOf(
            KEY_NAME to destination.name,
            KEY_NAV_ID to navId,
            KEY_NAV_ARGS to navArgs,
            KEY_FREE_ARGS to freeArgs,
            KEY_NAV_RESULT to navResult,
            KEY_SAVED_STATE to savedState,
            KEY_SAVED_NAV_CONTROLLERS to navControllersStorage.saveToSaveState()
        )
    }

    internal fun close() {
        // close navControllers
        navControllersStorage.close()
        // stop and clear viewModels
        viewModels.map { it.value }.onEach { it.close() }
        viewModels.clear()
    }
}

/**
 * Converts [NavDestination] into [NavEntry]
 *
 * @param navArgs entry navArgs
 * @param freeArgs entry freeArgs
 * @param navResult entry navResult
 */
public fun <Args> NavDestination<Args>.toNavEntry(
    navArgs: Args? = null,
    freeArgs: Any? = null,
    navResult: Any? = null
): NavEntry<Args> = NavEntry(
    destination = this,
    navArgs = navArgs,
    freeArgs = freeArgs,
    navResult = navResult
)