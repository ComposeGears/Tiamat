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
 * The NavController manages a back stack of destinations that represent the
 * navigation history. It provides methods to navigate between destinations,
 * handle back navigation, and manage the back stack.
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
        private const val KEY_CURRENT = "current"
        private const val KEY_BACK_STACK = "backStack"

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

        @Suppress("UNCHECKED_CAST")
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
            val current = (savedState[KEY_CURRENT] as? SavedState)
                ?.let { NavEntry.restoreFromSavedState(navController, it) }
            val backStackItems = savedState[KEY_BACK_STACK] as? List<SavedState>
            val backStack = backStackItems?.map { item ->
                NavEntry.restoreFromSavedState(navController, item)
            }
            if (backStack != null && backStack.isNotEmpty()) {
                navController.editBackStack { backStack.onEach { add(it) } }
            }
            if (current != null) {
                navController.navigate(current)
            }
            navController.parent = parent
            return navController
        }
    }

    // ----------- internal properties ---------------------------------------------------------------------------------

    private var internalCurrentTransitionFlow: MutableStateFlow<Transition?> = MutableStateFlow(null)
    private var internalCurrentBackStackFlow: MutableStateFlow<List<NavEntry<*>>> = MutableStateFlow(emptyList())
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
     * Flow of the current navigation transition.
     * Emits a new value when navigation occurs.
     */
    public val currentTransitionFlow: StateFlow<Transition?> = internalCurrentTransitionFlow

    /**
     * Flow of the current back stack.
     * Emits a new value when the back stack changes.
     */
    public val currentBackStackFlow: StateFlow<List<NavEntry<*>>> = internalCurrentBackStackFlow

    // ----------- public methods --------------------------------------------------------------------------------------

    /**
     * Saves the current state of the NavController.
     *
     * @return A SavedState object representing the NavController's current state
     */
    public fun saveToSavedState(): SavedState = SavedState(
        KEY_KEY to key,
        KEY_SAVEABLE to saveable,
        KEY_CURRENT to getCurrentNavEntry()?.saveToSavedState(),
        KEY_BACK_STACK to getBackStack().map { it.saveToSavedState() }
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
     * Gets the current navigation entry.
     *
     * @return The current navigation entry, or null if there is none
     */
    public fun getCurrentNavEntry(): NavEntry<*>? = currentTransitionFlow.value?.targetEntry

    /**
     * Gets the current back stack.
     *
     * @return A list of the navigation entries in the back stack
     */
    public fun getBackStack(): List<NavEntry<*>> = internalCurrentBackStackFlow.value

    /**
     * Checks if the NavController can navigate back.
     *
     * @return True if the back stack is not empty, false otherwise
     */
    public fun hasBackEntries(): Boolean = getBackStack().isNotEmpty()

    /**
     * Edits the back stack using the provided actions.
     *
     * @param actions The actions to perform on the back stack
     */
    public fun editBackStack(actions: BackStackEditScope.() -> Unit) {
        BackStackEditScope(getBackStack())
            .apply(actions)
            .let { updateBackStackInternal(it.backStack) }
    }

    // ----------- navigation methods ----------------------------------------------------------------------------------

    /**
     * Navigates to a new destination.
     * The current destination is added to the back stack.
     *
     * @param entry The navigation entry to navigate to
     * @param transitionData Optional data to customize the transition animation
     */
    internal fun <Args : Any> navigate(
        entry: NavEntry<Args>,
        transitionData: Any? = null,
    ) {
        entry.ensureDetachedAndAttach()
        val currentNavEntry = getCurrentNavEntry()
        currentNavEntry?.let { editBackStack { addWithoutAttach(it) } }
        updateCurrentNavEntryInternal(
            from = currentNavEntry,
            to = entry,
            isForward = true,
            transitionData = transitionData
        )
    }

    /**
     * Replaces the current destination with a new one.
     * The current destination is not added to the back stack.
     *
     * @param entry The navigation entry to replace with
     * @param transitionData Optional data to customize the transition animation
     */
    internal fun <Args : Any> replace(
        entry: NavEntry<Args>,
        transitionData: Any? = null,
    ) {
        entry.ensureDetachedAndAttach()
        val currentNavEntry = getCurrentNavEntry()
        currentNavEntry?.detachFromNavController()
        updateCurrentNavEntryInternal(
            from = currentNavEntry,
            to = entry,
            isForward = true,
            transitionData = transitionData
        )
    }

    /**
     * Pops the back stack to an existing destination, or navigates to it if not in the back stack.
     *
     * @param dest The destination to pop to
     * @param transitionData Optional data to customize the transition animation
     * @param orElse Action to perform if the destination is not found in the back stack
     */
    internal fun <Args : Any> popToTop(
        dest: NavDestination<Args>,
        transitionData: Any? = null,
        orElse: NavController.() -> Unit = {
            navigate(dest.toNavEntry(), transitionData)
        }
    ) {
        val existingEntry = getBackStack().firstOrNull { it.destination == dest }
        if (existingEntry != null) {
            val currentNavEntry = getCurrentNavEntry()
            editBackStack {
                removeWithoutDetach(existingEntry)
                currentNavEntry?.let { addWithoutAttach(it) }
            }
            updateCurrentNavEntryInternal(
                from = currentNavEntry,
                to = existingEntry,
                isForward = true,
                transitionData = transitionData
            )
        } else orElse()
    }

    /**
     * Navigates back in the navigation hierarchy.
     *
     * @param to Optional destination to navigate back to
     * @param result Optional result to pass to the destination
     * @param inclusive Whether to include the destination in the back operation
     * @param recursive Whether to recursively navigate back if current back operation impossible
     * @param transitionData Optional data to customize the transition animation
     * @return True if back navigation was handled, false otherwise
     */
    internal fun back(
        to: NavDestination<*>? = null,
        result: Any? = null,
        inclusive: Boolean = false,
        recursive: Boolean = true,
        transitionData: Any? = null,
    ): Boolean {
        val backStack = getBackStack()
        val targetIndex =
            if (to != null) backStack
                .indexOfLast { it.destination == to }
                .let { if (inclusive) it - 1 else it }
            else backStack.size - 1
        return when {
            targetIndex >= 0 -> {
                val currentNavEntry = getCurrentNavEntry()
                val targetNavEntry = backStack[targetIndex]
                targetNavEntry.setNavResult(result)
                currentNavEntry?.detachFromNavController()
                editBackStack {
                    while (this.backStack.lastIndex != targetIndex) removeLast()
                    removeWithoutDetach(targetNavEntry)
                }
                updateCurrentNavEntryInternal(currentNavEntry, targetNavEntry, false, transitionData)
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
                is Route.Destination -> UnresolvedDestination(element.name).toNavEntry()
                is Route.NavController -> null
            }?.also { entry: NavEntry<*> ->
                elements.removeAt(0)
                pendingStack.add(entry)
            } ?: break
        } while (elements.isNotEmpty())

        if (pendingStack.isEmpty()) error("Route: no start entry for NavController:$key")
        val pendingCurrentEntry = pendingStack.removeAt(pendingStack.lastIndex)
        // run route for nested NavController
        if (elements.isNotEmpty()) elements
            .removeAt(0)
            .let {
                val ncData = it as Route.NavController
                val nc = create(ncData.key, ncData.saveable ?: saveable, this)
                pendingCurrentEntry.navControllerStore.add(nc)
                nc.route(Route(elements))
            }
        // apply pending stack
        editBackStack {
            clear()
            pendingStack.forEach { add(it) }
        }
        replace(pendingCurrentEntry)
    }

    // ----------- internal helpers methods ------------------------------------------------------------------------------------

    private fun updateCurrentNavEntryInternal(
        from: NavEntry<*>?,
        to: NavEntry<*>?,
        isForward: Boolean,
        transitionData: Any?
    ) {
        internalCurrentTransitionFlow.tryEmit(
            Transition(
                targetEntry = to,
                transitionData = transitionData,
                isForward = isForward
            )
        )
        onNavigationListener?.onNavigate(from, to, isForward)
    }

    private fun updateBackStackInternal(newBackStack: List<NavEntry<*>>) {
        internalCurrentBackStackFlow.tryEmit(newBackStack)
    }

    internal fun close() {
        getCurrentNavEntry()?.detachFromNavController()
        internalCurrentTransitionFlow.tryEmit(null)
        getBackStack().onEach { it.detachFromNavController() }
        updateBackStackInternal(emptyList())
    }

    // ----------- other -----------------------------------------------------------------------------------------------

    @ExcludeFromTests
    override fun toString(): String =
        "NavController(key=$key, current=${getCurrentNavEntry()}, parent=${parent?.key}}"

    // ----------- support classes -------------------------------------------------------------------------------------

    /**
     * Represents a transition between navigation entries.
     *
     * @property targetEntry The navigation entry being navigated to
     * @property transitionData Optional data to customize the transition
     * @property isForward Whether the transition is moving forward (true) or backward (false) in the navigation stack
     */
    public data class Transition(
        val targetEntry: NavEntry<*>?,
        val transitionData: Any?,
        val isForward: Boolean,
    )

    /**
     * Scope for editing the back stack.
     */
    public class BackStackEditScope internal constructor(
        initialBackStack: List<NavEntry<*>>
    ) {

        private val internalEditableBackStack = initialBackStack.toMutableList()

        /**
         * The current back stack.
         */
        public val backStack: List<NavEntry<*>> = internalEditableBackStack

        // used internally to add current entry into backstack
        internal fun addWithoutAttach(entry: NavEntry<*>) {
            internalEditableBackStack.add(entry)
        }

        // used internally to remove entry from backstack in order to make it current
        internal fun removeWithoutDetach(entry: NavEntry<*>) {
            internalEditableBackStack.remove(entry)
        }

        /**
         * Adds a navigation entry to the back stack.
         *
         * @param entry The navigation entry to add.
         */
        public fun <Args : Any> add(
            entry: NavEntry<Args>,
        ) {
            entry.ensureDetachedAndAttach()
            internalEditableBackStack.add(entry)
        }

        /**
         * Adds a destination to the back stack.
         *
         * @param dest The destination to add.
         * @param navArgs The navigation navArgs.
         * @param freeArgs The navigation freeArgs.
         */
        public fun <Args : Any> add(
            dest: NavDestination<Args>,
            navArgs: Args? = null,
            freeArgs: Any? = null,
        ) {
            add(dest.toNavEntry(navArgs = navArgs, freeArgs = freeArgs))
        }

        /**
         * Adds a navigation entry to the back stack at the specified index.
         *
         * @param index The index to add the entry at.
         * @param entry The navigation entry to add.
         */
        public fun <Args : Any> add(
            index: Int,
            entry: NavEntry<Args>,
        ) {
            entry.ensureDetachedAndAttach()
            internalEditableBackStack.add(index, entry)
        }

        /**
         * Adds a destination to the back stack at the specified index.
         *
         * @param index The index to add the destination at.
         * @param dest The destination to add.
         * @param navArgs The navigation navArgs.
         * @param freeArgs The navigation freeArgs.
         */
        public fun <Args : Any> add(
            index: Int,
            dest: NavDestination<Args>,
            navArgs: Args? = null,
            freeArgs: Any? = null,
        ) {
            add(index, dest.toNavEntry(navArgs = navArgs, freeArgs = freeArgs))
        }

        /**
         * Removes the navigation entry at the specified index.
         *
         * @param index The index of the entry to remove.
         */
        public fun removeAt(index: Int) {
            internalEditableBackStack.removeAt(index).detachFromNavController()
        }

        /**
         * Removes the last navigation entry from the back stack.
         *
         * @return `true` if the entry was successfully removed, `false` otherwise.
         */
        public fun removeLast(): Boolean {
            return if (backStack.isNotEmpty()) {
                internalEditableBackStack.removeAt(backStack.lastIndex).detachFromNavController()
                true
            } else false
        }

        /**
         * Removes the last navigation entry for the specified destination.
         *
         * @param dest The destination to remove the last entry for.
         * @return `true` if the entry was successfully removed, `false` otherwise.
         */
        public fun removeLast(dest: NavDestination<*>): Boolean {
            val ind = backStack.indexOfLast { it.destination.name == dest.name }
            if (ind >= 0) internalEditableBackStack.removeAt(ind).detachFromNavController()
            return ind >= 0
        }

        /**
         * Removes the last navigation entry that matches the specified predicate.
         *
         * @param predicate The predicate to match entries against.
         * @return `true` if the entry was successfully removed, `false` otherwise.
         */
        public fun removeLast(predicate: (NavEntry<*>) -> Boolean): Boolean {
            val ind = backStack.indexOfLast(predicate)
            if (ind >= 0) internalEditableBackStack.removeAt(ind).detachFromNavController()
            return ind >= 0
        }

        /**
         * Removes all navigation entries that match the specified predicate.
         *
         * @param predicate The predicate to match entries against.
         */
        public fun removeAll(predicate: (NavEntry<*>) -> Boolean) {
            var i = 0
            while (i < backStack.size) {
                if (predicate(backStack[i])) {
                    internalEditableBackStack.removeAt(i).detachFromNavController()
                } else i++
            }
        }

        /**
         * Sets the back stack to the specified destinations.
         * This clears the current back stack before adding the new destinations.
         *
         * @param destinations The destinations to set.
         */
        public fun set(vararg destinations: NavDestination<*>) {
            clear()
            destinations.onEach { add(it) }
        }

        /**
         * Sets the back stack to the specified entries.
         * This clears the current back stack before adding the new entries.
         *
         * @param entries The entries to set.
         */
        public fun set(vararg entries: NavEntry<*>) {
            clear()
            entries.onEach { add(it) }
        }

        /**
         * Clears the back stack.
         */
        public fun clear() {
            while (backStack.isNotEmpty()) {
                internalEditableBackStack.removeAt(backStack.lastIndex).detachFromNavController()
            }
        }

        /**
         * Gets the size of the back stack.
         *
         * @return The size of the back stack.
         */
        public fun size(): Int = backStack.size
    }

    /**
     * Interface for listening to navigation transitions between screens.
     */
    public fun interface OnNavigationListener {
        /**
         * Called when a navigation transition occurs.
         *
         * @param from The [NavEntry] being navigated from
         * @param to The [NavEntry] being navigated to
         * @param isForward True if navigating forward, false if navigating backward
         */
        public fun onNavigate(from: NavEntry<*>?, to: NavEntry<*>?, isForward: Boolean)
    }
}