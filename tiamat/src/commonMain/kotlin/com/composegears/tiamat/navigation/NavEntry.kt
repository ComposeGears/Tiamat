package com.composegears.tiamat.navigation

import androidx.compose.runtime.Stable
import com.composegears.tiamat.ExcludeFromTests
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents a navigation entry in the navigation stack.
 *
 * A NavEntry is a combination of a destination and its associated navigation arguments.
 * It can also store results, free arguments, and maintain view models.
 *
 * @param destination The navigation destination this entry represents
 * @param navArgs Optional typed arguments to pass to the destination
 * @param freeArgs Optional untyped arguments to pass to the destination
 * @param navResult Optional result value for this entry
 */
@Stable
public class NavEntry<Args> public constructor(
    destination: NavDestination<Args>,
    navArgs: Args? = null,
    freeArgs: Any? = null,
    navResult: Any? = null
) : RouteElement {

    public companion object {

        private const val KEY_DESTINATION = "destination"
        private const val KEY_UUID = "uuid"
        private const val KEY_NAV_ARGS = "navArgs"
        private const val KEY_FREE_ARGS = "freeArgs"
        private const val KEY_NAV_RESULT = "navResult"
        private const val KEY_SAVED_STATE = "savedState"
        private const val KEY_NAV_CONTROLLERS = "navControllers"

        @Suppress("UNCHECKED_CAST")
        internal fun restoreFromSavedState(
            parent: NavController?,
            savedState: SavedState
        ): NavEntry<*> {
            val destination = savedState[KEY_DESTINATION] as? String
                ?: error("Unable to restore NavEntry: destination is null")
            val uuid = savedState[KEY_UUID] as? String
                ?: error("Unable to restore NavEntry: uuid is null")
            val navArgs = savedState[KEY_NAV_ARGS]
            val freeArgs = savedState[KEY_FREE_ARGS]
            val navResult = savedState[KEY_NAV_RESULT]
            val entrySavedState = savedState[KEY_SAVED_STATE] as? SavedState
            val navControllers = savedState[KEY_NAV_CONTROLLERS] as? SavedState
            return NavEntry(
                destination = UnresolvedDestination(destination),
                navArgs = navArgs,
                freeArgs = freeArgs,
                navResult = navResult,
            ).also {
                it.uuid = uuid
                it.savedState = entrySavedState
                it.navControllersStorage.loadFromSavedState(parent, navControllers)
            }
        }
    }

    // used to get current SavedState when attached to UI
    private var savedStateSaver: (() -> SavedState)? = null

    internal val navControllersStorage: NavControllersStorage = NavControllersStorage()
    internal val viewModelsStorage: ViewModelsStorage = ViewModelsStorage()
    internal var savedState: SavedState? = null

    internal var isAttachedToNavController = false
        private set
    internal var isAttachedToUI = false
        private set
    internal var isResolved = destination !is UnresolvedDestination
        private set

    @OptIn(ExperimentalUuidApi::class)
    internal var uuid: String = Uuid.random().toHexString()
        private set

    /**
     * The destination this entry represents.
     */
    public var destination: NavDestination<Args> = destination
        private set

    /**
     * Typed arguments passed to the destination.
     */
    public var navArgs: Args? = navArgs
        internal set

    /**
     * Untyped arguments passed to the destination.
     */
    public var freeArgs: Any? = freeArgs
        internal set

    /**
     * Result value for this navigation entry.
     */
    public var navResult: Any? = navResult
        internal set

    @Suppress("UNCHECKED_CAST")
    internal fun resolveDestination(destinationResolver: (name: String) -> NavDestination<*>?) {
        destination = destinationResolver(destination.name)
            ?.let { it as? NavDestination<Args> }
            ?: error("Unable to resolve destination: ${destination.name}")
        isResolved = true
    }

    internal fun saveToSavedState(): SavedState {
        if (savedStateSaver != null) savedState = savedStateSaver!!()
        return SavedState(
            KEY_DESTINATION to destination.name,
            KEY_UUID to uuid,
            KEY_NAV_ARGS to navArgs,
            KEY_FREE_ARGS to freeArgs,
            KEY_NAV_RESULT to navResult,
            KEY_SAVED_STATE to savedState,
            KEY_NAV_CONTROLLERS to navControllersStorage.saveToSavedState(),
        )
    }

    internal fun setSavedStateSaver(saver: (() -> SavedState)?) {
        savedStateSaver = saver
    }

    internal fun attachToNavController() {
        isAttachedToNavController = true
    }

    internal fun detachFromNavController() {
        isAttachedToNavController = false
        if (!isAttachedToUI) close()
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
        if (!isAttachedToNavController) close()
    }

    private fun close() {
        viewModelsStorage.clear()
        navControllersStorage.clear()
    }

    public fun contentKey(): String = "${destination.name}-$uuid"

    @ExcludeFromTests
    override fun toString(): String =
        "NavEntry(destination=${destination.name})"
}