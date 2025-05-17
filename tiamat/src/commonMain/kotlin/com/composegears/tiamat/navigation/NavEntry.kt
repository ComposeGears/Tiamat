package com.composegears.tiamat.navigation

import androidx.compose.runtime.Stable

@Stable
public class NavEntry<Args> public constructor(
    destination: NavDestination<Args>,
    navArgs: Args? = null,
    freeArgs: Any? = null,
    navResult: Any? = null
) : RouteElement {

    public companion object {

        private const val KEY_DESTINATION = "destination"
        private const val KEY_NAV_ARGS = "navArgs"
        private const val KEY_FREE_ARGS = "freeArgs"
        private const val KEY_NAV_RESULT = "navResult"
        private const val KEY_SAVED_STATE = "savedState"
        private const val KEY_NAV_CONTROLLERS = "navControllers"

        private var globalUID = 0L

        @Suppress("UNCHECKED_CAST")
        public fun restoreFromSavedState(
            parent: NavController?,
            savedState: SavedState
        ): NavEntry<*> {
            val destination = savedState[KEY_DESTINATION]?.toString()
                ?: error("Unable to restore NavEntry: destination is null")
            val navArgs = savedState[KEY_NAV_ARGS]
            val freeArgs = savedState[KEY_FREE_ARGS]
            val navResult = savedState[KEY_NAV_RESULT]
            val entrySavedState = savedState[KEY_SAVED_STATE] as? SavedState?
            val navControllers = savedState[KEY_NAV_CONTROLLERS] as? SavedState?
            return NavEntry(
                destination = UnresolvedDestination(destination),
                navArgs = navArgs,
                freeArgs = freeArgs,
                navResult = navResult,
            ).also {
                it.savedState = entrySavedState
                it.navControllersStorage.loadFromSavedState(parent, navControllers)
            }
        }
    }

    // used to get current SavedState when attached to UI
    internal var savedStateSaver: (() -> SavedState)? = null
    internal var isAttachedToNavController = false
    internal var isAttachedToUI = false

    public val uid: Long = globalUID++
    public var destination: NavDestination<Args> = destination
        internal set
    public var navArgs: Args? = navArgs
        internal set
    public var freeArgs: Any? = freeArgs
        internal set
    public var navResult: Any? = navResult
        internal set
    public var savedState: SavedState? = null
        internal set
    public val navControllersStorage: NavControllersStorage = NavControllersStorage()
    public val viewModelsStorage: ViewModelsStorage = ViewModelsStorage()

    internal fun isResolved() = destination !is UnresolvedDestination

    @Suppress("UNCHECKED_CAST")
    internal fun resolveDestination(destinations: Array<NavDestination<*>>) {
        destination = destinations
            .firstOrNull { it.name == destination.name }
            ?.let { it as? NavDestination<Args> }
            ?: error("Unable to resolve destination: ${destination.name}")
    }

    internal fun saveToSavedState(): SavedState = SavedState(
        KEY_DESTINATION to destination.name,
        KEY_NAV_ARGS to navArgs,
        KEY_FREE_ARGS to freeArgs,
        KEY_NAV_RESULT to navResult,
        KEY_SAVED_STATE to (savedStateSaver?.invoke() ?: savedState),
        KEY_NAV_CONTROLLERS to navControllersStorage.saveToSavedState(),
    )

    internal fun attachToNavController() {
        isAttachedToNavController = true
    }

    internal fun detachFromNavController() {
        isAttachedToNavController = false
        if (!isAttachedToNavController && !isAttachedToUI) close()
    }

    internal fun ensureDetachedAndAttach() {
        if (isAttachedToNavController) error("NavEntry is already attached to a NavController")
        attachToNavController()
    }

    internal fun attachToUI() {
        isAttachedToUI = true
    }

    internal fun detachFromUI() {
        isAttachedToUI = false
        if (!isAttachedToNavController && !isAttachedToUI) close()
    }

    private fun close() {
        viewModelsStorage.clear()
        navControllersStorage.clear()
    }
}