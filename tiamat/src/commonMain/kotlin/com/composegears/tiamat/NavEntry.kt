package com.composegears.tiamat

import androidx.compose.runtime.Stable

/**
 * Hold nav entry information
 */
@Stable
class NavEntry<Args> private constructor(
    val destination: NavDestination<Args>,
    navArgs: Args? = null,
    freeArgs: Any? = null,
    navResult: Any? = null,
    savedState: Map<String, List<Any?>>? = null,
    savedNavControllers: List<Map<String, Any?>>? = null,
) {

    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_NAV_ID = "id"
        private const val KEY_NAV_ARGS = "navArgs"
        private const val KEY_FREE_ARGS = "freeArgs"
        private const val KEY_NAV_RESULT = "navResult"
        private const val KEY_SAVED_STATE = "savedState"
        private const val KEY_SAVED_NAV_CONTROLLERS = "savedNavControllers"

        internal fun restore(
            data: Map<String, Any?>,
            destinations: Array<NavDestination<*>>,
        ): NavEntry<*> {
            val destination = (data[KEY_NAME] as String).let { name -> destinations.first { it.name == name } }
            return restore(data, destination)
        }

        @Suppress("UNCHECKED_CAST")
        private fun <Args> restore(
            data: Map<String, Any?>,
            destination: NavDestination<Args>,
        ) = NavEntry(
            destination = destination,
            navArgs = data[KEY_NAV_ARGS] as Args,
            freeArgs = data[KEY_FREE_ARGS],
            navResult = data[KEY_NAV_RESULT],
            savedState = data[KEY_SAVED_STATE] as? Map<String, List<Any?>>?,
            savedNavControllers = data[KEY_SAVED_NAV_CONTROLLERS] as? List<Map<String, Any?>>?
        ).also {
            it.navId = data[KEY_NAV_ID] as Long
        }
    }

    var navArgs: Args? = navArgs
        internal set
    var freeArgs: Any? = freeArgs
        internal set
    var navResult: Any? = navResult
        internal set

    internal var navId: Long = -1
    internal val viewModels = mutableMapOf<String, TiamatViewModel>()
    internal var savedState: Map<String, List<Any?>> = savedState ?: emptyMap()
    internal var savedStateSaver: (() -> Map<String, List<Any?>>)? = null
    internal val navControllersStorage = NavControllersStorage().apply { restoreFromSavedState(savedNavControllers) }

    constructor(
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
        navControllersStorage.save()
    }

    internal fun saveToSaveState(): Map<String, Any?> {
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
        viewModels.map { it.value }.toList().onEach { it.close() }
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
fun <Args> NavDestination<Args>.toNavEntry(
    navArgs: Args? = null,
    freeArgs: Any? = null,
    navResult: Any? = null
) = NavEntry(
    destination = this,
    navArgs = navArgs,
    freeArgs = freeArgs,
    navResult = navResult
)