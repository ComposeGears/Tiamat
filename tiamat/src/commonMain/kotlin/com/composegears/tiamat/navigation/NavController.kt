package com.composegears.tiamat.navigation

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.composegears.tiamat.ExcludeFromTests
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Controls navigation between screens in an application.
 *
 * The NavController manages a nav stack of destinations that represent the
 * navigation history. It provides methods to navigate between destinations,
 * handle back navigation, and manage the navigation stack.
 *
 * @property key An optional identifier for this NavController
 * @property saveable Whether this NavController's state should be saved and restored
 */
public class NavController internal constructor(
    public val key: String?,
    public val saveable: Boolean,
) : ViewModelStoreOwner {
    public companion object {

        private const val KEY_KEY = "key"
        private const val KEY_SAVEABLE = "saveable"
        private const val KEY_NAV_STACK = "navStack"

        public fun create(
            key: String? = null,
            saveable: Boolean = true,
            parent: NavController? = null,
            startDestination: NavDestination<*>,
            config: NavController.() -> Unit = {}
        ): NavController = create(
            key = key,
            saveable = saveable,
            parent = parent,
            startEntry = startDestination.toNavEntry(),
            config = config
        )

        public fun create(
            key: String? = null,
            saveable: Boolean = true,
            parent: NavController? = null,
            startEntry: NavEntry<*>? = null,
            config: NavController.() -> Unit = {}
        ): NavController {
            val navController = NavController(key, saveable)
            if (startEntry != null) navController.navigate(startEntry)
            navController.parent = parent
            navController.config()
            return navController
        }

        @Suppress("UNCHECKED_CAST")
        public fun restoreFromSavedState(
            parent: NavController? = null,
            savedState: SavedState,
        ): NavController {
            val navController = NavController(
                key = savedState[KEY_KEY] as? String,
                saveable = savedState[KEY_SAVEABLE] as Boolean
            )
            val navStackItems = savedState[KEY_NAV_STACK] as? List<SavedState>
            val navStack = navStackItems?.map { item ->
                NavEntry.restoreFromSavedState(navController, item)
            }
            if (navStack != null && navStack.isNotEmpty()) {
                navController.editNavStack(null, TransitionType.Instant) { _ -> navStack }
            }
            navController.parent = parent
            return navController
        }
    }

    // ----------- internal properties ---------------------------------------------------------------------------------

    private var internalNavStateFlow: MutableStateFlow<NavState> = MutableStateFlow(
        NavState(
            transitionData = null,
            transitionType = TransitionType.Instant,
            stack = emptyList()
        )
    )
    private var onNavigationListener: OnNavigationListener? = null

    // ----------- public properties -----------------------------------------------------------------------------------

    /**
     * Returns the [ViewModelStore] associated with this NavController.
     */
    public override val viewModelStore: ViewModelStore = ViewModelStore()

    /**
     * The parent NavController in a hierarchical navigation structure.
     */
    public var parent: NavController? = null
        private set

    /**
     * Flow of the current navigation state.
     * Emits a new value when navigation occurs or the stack is changed.
     */
    public val navStateFlow: StateFlow<NavState> = internalNavStateFlow

    // ----------- public methods --------------------------------------------------------------------------------------

    /**
     * Saves the current state of the NavController.
     *
     * @return A SavedState object representing the NavController's current state
     */
    public fun saveToSavedState(): SavedState = SavedState(
        KEY_KEY to key,
        KEY_SAVEABLE to saveable,
        KEY_NAV_STACK to getNavStack().map { it.saveToSavedState() }
    )

    /**
     * Sets a listener to be notified when navigation occurs.
     *
     * @param listener The listener to be notified, or null to remove any existing listener
     */
    public fun setOnNavigationListener(listener: OnNavigationListener?) {
        onNavigationListener = listener
    }

    /**
     * Finds a parent NavController with the specified key.
     *
     * @param key The key of the parent NavController to find
     * @return The parent NavController with the specified key, or null if not found
     */
    public fun findParentNavController(key: String): NavController? {
        var nc: NavController? = this
        while (nc != null) {
            if (nc.key == key) return nc
            else nc = nc.parent
        }
        return null
    }

    /**
     * Gets the current(last) navigation entry.
     *
     * @return The current navigation entry, or null if there is none
     */
    public fun getCurrentNavEntry(): NavEntry<*>? = internalNavStateFlow.value.stack.lastOrNull()

    /**
     * Gets the entire navigation stack.
     *
     * @return A list of the navigation entries in the stack
     */
    public fun getNavStack(): List<NavEntry<*>> = internalNavStateFlow.value.stack

    /**
     * Checks if it is possible to navigate back.
     *
     * @return `true` if the nav stack contains an entry to back to, `false` otherwise.
     */
    public fun canNavigateBack(): Boolean = getNavStack().size > 1

    // ----------- navigation methods ----------------------------------------------------------------------------------

    /**
     * Edits the navigation stack using the provided editor.
     *
     * @param transitionData Optional data to customize the transition data
     * @param transitionType The type of transition
     * @param editor A function that takes the current nav stack and returns a new nav stack
     */
    internal fun editNavStack(
        transitionData: Any?,
        transitionType: TransitionType,
        editor: (List<NavEntry<*>>) -> List<NavEntry<*>>
    ) {
        val oldStack = getNavStack().toMutableList()
        val newStack = editor(oldStack)
        if (newStack != oldStack) {
            newStack.onEach { entry ->
                val index = oldStack.indexOf(entry)
                // old item - do nothing, new item - attach
                if (index >= 0) oldStack.removeAt(index)
                else entry.ensureDetachedAndAttach()
            }
            oldStack.onEach { it.detachFromNavController() } // removed items - detach
            updateNavState(
                transitionData = transitionData,
                transitionType = transitionType,
                stack = newStack
            )
        }
    }

    /**
     * Navigates to a new destination.
     * The current destination is added to the nav stack.
     *
     * @param entry The navigation entry to navigate to
     * @param transitionData Optional data to customize the transition animation
     */
    internal fun <Args : Any> navigate(
        entry: NavEntry<Args>,
        transitionData: Any? = null,
    ) {
        entry.ensureDetachedAndAttach()
        updateNavState(
            transitionData = transitionData,
            transitionType = TransitionType.Forward,
            stack = getNavStack() + entry
        )
    }

    /**
     * Replaces the current destination with a new one.
     * The current destination is not added to the nav stack.
     *
     * @param entry The navigation entry to replace with
     * @param transitionData Optional data to customize the transition animation
     */
    internal fun <Args : Any> replace(
        entry: NavEntry<Args>,
        transitionData: Any? = null,
    ) {
        val navStack = getNavStack()
        navStack.lastOrNull()?.detachFromNavController()
        entry.ensureDetachedAndAttach()
        updateNavState(
            transitionData = transitionData,
            transitionType = TransitionType.Forward,
            stack = navStack.toMutableList().apply {
                removeLastOrNull()
                add(entry)
            }
        )
    }

    /**
     * Pops the nav stack to an existing destination.
     *
     * @param dest The destination to pop to
     * @param transitionData Optional data to customize the transition animation
     * @param orElse Action to perform if the destination is not found in the nav stack, defaults to navigating to it
     */
    internal fun <Args : Any> popToTop(
        dest: NavDestination<Args>,
        transitionData: Any? = null,
        orElse: NavController.() -> Unit
    ) {
        val navStack = getNavStack()
        val entryIndex = navStack.indexOfLast { it.destination == dest }
        if (entryIndex >= 0) updateNavState(
            transitionData = transitionData,
            transitionType = TransitionType.Forward,
            stack = navStack.toMutableList().apply {
                add(removeAt(entryIndex))
            }
        ) else orElse()
    }

    /**
     * Navigates back in the navigation hierarchy.
     *
     * @param to Optional destination to navigate back to
     * @param result Optional result to pass to the destination
     * @param inclusive Whether to include the destination in the back operation
     * @param recursive Whether to recursively navigate back if current back operation impossible
     * @param transitionData Optional data to customize the transition
     * @return True if back navigation was handled, false otherwise
     */
    internal fun back(
        to: NavDestination<*>? = null,
        result: Any? = null,
        inclusive: Boolean = false,
        recursive: Boolean = true,
        transitionData: Any? = null,
    ): Boolean {
        val navStack = getNavStack()
        val targetIndex =
            if (to != null) navStack
                .dropLast(1)
                .indexOfLast { it.destination == to }
                .let { if (inclusive) it - 1 else it }
            else navStack.lastIndex - 1
        return when {
            targetIndex >= 0 -> {
                navStack[targetIndex].setNavResult(result)
                for (i in targetIndex + 1..navStack.lastIndex) {
                    navStack[i].detachFromNavController()
                }
                updateNavState(
                    transitionData = transitionData,
                    transitionType = TransitionType.Backward,
                    stack = navStack.subList(0, targetIndex + 1)
                )
                true
            }
            recursive ->
                parent?.back(
                    to = to,
                    result = result,
                    inclusive = inclusive,
                    recursive = recursive,
                    transitionData = transitionData
                ) ?: false
            else -> false
        }
    }

    /**
     * Navigates using a route builder.
     *
     * @param routeBuilder Builder function to construct the route
     */
    @TiamatExperimentalApi
    public fun route(routeBuilder: Route.() -> Unit) {
        route(Route(routeBuilder))
    }

    /**
     * Navigates using a provided route.
     *
     * @param route The route to navigate
     */
    @TiamatExperimentalApi
    @Suppress("CyclomaticComplexMethod")
    public fun route(route: Route) {
        val elements =
            if (route.elements.isEmpty()) error("Route is empty")
            else route.elements.toMutableList()
        val pendingStack = mutableListOf<NavEntry<*>>()
        // process entries till the end or NavController
        do {
            val element = elements[0]
            when (element) {
                is NavEntry<*> -> element
                is NavDestination<*> -> element.toNavEntry()
                is Route.Destination -> NavDestination.Unresolved(element.name).toNavEntry()
                is Route.NavController -> null
            }?.also { entry: NavEntry<*> ->
                elements.removeAt(0)
                pendingStack.add(entry)
            } ?: break
        } while (elements.isNotEmpty())

        if (pendingStack.isEmpty()) error("Route: no start entry for NavController:$key")
        // run route for nested NavController
        if (elements.isNotEmpty()) elements
            .removeAt(0)
            .let {
                val ncData = it as Route.NavController
                val nc = create(ncData.key, ncData.saveable ?: saveable, this)
                pendingStack.last().navControllerStore.add(nc)
                nc.route(Route(elements))
            }
        // apply pending stack
        editNavStack(null, TransitionType.Forward) { _ -> pendingStack }
    }

    // ----------- internal helpers methods ------------------------------------------------------------------------------------

    internal fun resolveNavDestinations(
        destinationResolver: (name: String) -> NavDestination<*>?,
    ) {
        getNavStack().onEach {
            if (!it.isResolved)
                it.resolveDestination(destinationResolver)
        }
    }

    private fun updateNavState(
        transitionData: Any?,
        transitionType: TransitionType,
        stack: List<NavEntry<*>>
    ) {
        val currentNavEntry = getCurrentNavEntry()
        internalNavStateFlow.tryEmit(
            NavState(
                transitionData = transitionData,
                transitionType = transitionType,
                stack = stack
            )
        )
        onNavigationListener?.onNavigate(currentNavEntry, stack.lastOrNull(), transitionType)
    }

    internal fun close() {
        getNavStack().onEach { it.detachFromNavController() }
        internalNavStateFlow.tryEmit(
            NavState(
                transitionData = null,
                transitionType = TransitionType.Instant,
                stack = emptyList()
            )
        )
    }

    // ----------- other -----------------------------------------------------------------------------------------------

    @ExcludeFromTests
    override fun toString(): String =
        "NavController(key=$key, current=${getCurrentNavEntry()}, parent=${parent?.key}}"

    // ----------- support classes -------------------------------------------------------------------------------------

    public enum class TransitionType {
        Forward,
        Backward,
        Instant
    }

    /**
     * Represents the state of the NavController navigation (stack and last/current transition info).
     *
     * @property transitionData Optional data associated with the last/current transition
     * @property stack The list of navigation entries in the stack
     */
    public data class NavState(
        val transitionData: Any?,
        val transitionType: TransitionType,
        val stack: List<NavEntry<*>> = emptyList(),
    )

    /**
     * Interface for listening to navigation transitions between screens.
     */
    public fun interface OnNavigationListener {
        /**
         * Called when a navigation transition occurs.
         *
         * @param from The [NavEntry] being navigated from
         * @param to The [NavEntry] being navigated to
         * @param type The type of transition
         */
        public fun onNavigate(from: NavEntry<*>?, to: NavEntry<*>?, type: TransitionType)
    }
}