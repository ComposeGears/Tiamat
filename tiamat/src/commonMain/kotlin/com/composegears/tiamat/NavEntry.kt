package com.composegears.tiamat

import androidx.compose.runtime.Stable

/**
 * Internal class
 *
 * Hold nav entry information and allow to save/restore state base on storage mode
 */
@Stable
class NavEntry<Args> private constructor(
    val destination: NavDestination<Args>,
    navArgs: Args? = null,
    freeArgs: Any? = null,
    navResult: Any? = null,
    savedState: Map<String, List<Any?>>? = null,
    viewModels: Map<String, TiamatViewModel>? = null,
    restoredNavControllers: List<NavController>? = null,
) {

    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_NAV_ARGS = "navArgs"
        private const val KEY_FREE_ARGS = "freeArgs"
        private const val KEY_NAV_RESULT = "navResult"
        private const val KEY_SAVED_STATE = "savedState"
        private const val KEY_VIEW_MODELS = "viewModels"
        private const val KEY_CHILD_NAV_CONTROLLERS = "childNavControllers"

//        internal fun restore(
//            data: Map<String, Any?>,
//            destinations: Array<NavDestination<*>>,
//        ): NavEntry<*> {
//            val destination = (data[KEY_NAME] as String).let { name -> destinations.first { it.name == name } }
//            return restore(data, destination)
//        }
//
//        @Suppress("UNCHECKED_CAST")
//        private fun <Args> restore(
//            data: Map<String, Any?>,
//            destination: NavDestination<Args>,
//        ) = NavEntry(
//            destination = destination,
//            navArgs = data[KEY_NAV_ARGS] as Args,
//            freeArgs = data[KEY_FREE_ARGS],
//            navResult = data[KEY_NAV_RESULT],
//            savedState = data[KEY_SAVED_STATE] as Map<String, List<Any?>>?,
//            viewModels = data[KEY_VIEW_MODELS] as Map<String, TiamatViewModel>?,
//            childNavControllers = data[KEY_CHILD_NAV_CONTROLLERS] as List<NavController>?
//        )
    }

    var navArgs: Args? = navArgs
        internal set
    var freeArgs: Any? = freeArgs
        internal set
    var navResult: Any? = navResult
        internal set

    internal val viewModels = viewModels?.toMutableMap() ?: mutableMapOf()
    internal var savedState: Map<String, List<Any?>> = savedState ?: emptyMap()
    internal val navControllersStorage = NavControllersStorage(restoredNavControllers)

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
        viewModels = null
    )

    internal constructor(
        navEntry: NavEntry<Args>
    ) : this(
        destination = navEntry.destination,
        navArgs = navEntry.navArgs,
        freeArgs = navEntry.freeArgs,
        navResult = navEntry.navResult,
        savedState = null,
        viewModels = null
    )

    internal fun attachNavController(navController: NavController) {
        navControllersStorage.attachNavController(navController)
    }

    internal fun detachNavController(navController: NavController) {
        navControllersStorage.detachNavController(navController)
    }

    internal fun saveState(saveState: Map<String, List<Any?>>) {
        this.savedState = saveState
        navControllersStorage.save()
    }

//    internal fun save() = mapOf(
//        KEY_NAME to destination.name,
//        KEY_NAV_ARGS to navArgs,
//        KEY_FREE_ARGS to freeArgs,
//        KEY_NAV_RESULT to navResult,
//        KEY_SAVED_STATE to savedState,
//        KEY_VIEW_MODELS to viewModels,
//        KEY_CHILD_NAV_CONTROLLERS to childNavControllers + activeChildNavControllers.map { it.save() }
//    )

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