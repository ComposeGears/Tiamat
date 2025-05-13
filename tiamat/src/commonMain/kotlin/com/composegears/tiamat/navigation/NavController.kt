package com.composegears.tiamat.navigation

import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// todo add NavEntry.close logic & tests
public class NavController internal constructor(
    public val key: String?,
    public val saveable: Boolean,
) : RouteElement {

    public companion object {

        private const val KEY_KEY = "key"
        private const val KEY_SAVEABLE = "saveable"
        private const val KEY_CURRENT = "current"
        private const val KEY_BACK_STACK = "backStack"

        public fun create(
            key: String? = null,
            saveable: Boolean,
            parent: NavController? = null,
            startDestination: NavDestination<*>? = null,
            config: NavController.() -> Unit = {}
        ): NavController = create(
            key = key,
            saveable = saveable,
            parent = parent,
            startDestination = startDestination?.toNavEntry(),
            config = config
        )

        @Suppress("UNCHECKED_CAST")
        public fun create(
            key: String? = null,
            saveable: Boolean,
            parent: NavController? = null,
            startDestination: NavEntry<*>? = null,
            config: NavController.() -> Unit = {}
        ): NavController {
            val navController = NavController(key, saveable)
            if (startDestination != null) navController.navigate(startDestination)
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
                key = savedState[KEY_KEY] as String?,
                saveable = savedState[KEY_SAVEABLE] as Boolean
            )
            val current = (savedState[KEY_CURRENT] as? SavedState)?.let { NavEntry.restoreFromSavedState(it) }
            val backStack = (savedState[KEY_BACK_STACK] as? List<*>)?.mapNotNull { item ->
                (item as? SavedState)?.let { NavEntry.restoreFromSavedState(it) }
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
    //todo add test for transition data is passed when nav with it
    private var internalCurrentTransitionFlow: MutableStateFlow<Transition?> = MutableStateFlow(null)
    private var internalCurrentBackStackFlow: MutableStateFlow<List<NavEntry<*>>> = MutableStateFlow(emptyList())
    private var onNavigationListener: OnNavigationListener? = null

    // ----------- public properties -----------------------------------------------------------------------------------

    public var parent: NavController? = null
        private set
    public val sharedViewModelsStorage: ViewModelsStorage = ViewModelsStorage()
    public val currentTransitionFlow: StateFlow<Transition?> = internalCurrentTransitionFlow
    public val currentBackStackFlow: StateFlow<List<NavEntry<*>>> = internalCurrentBackStackFlow

    // ----------- public methods --------------------------------------------------------------------------------------

    public fun saveToSavedState(): SavedState = SavedState(
        KEY_KEY to key,
        KEY_SAVEABLE to saveable,
        KEY_CURRENT to getCurrentNavEntry()?.saveToSavedState(),
        KEY_BACK_STACK to getBackStack().map { it.saveToSavedState() }
    )

    public fun setOnNavigationListener(listener: OnNavigationListener?) {
        onNavigationListener = listener
    }

    public fun findParentNavController(key: String): NavController? {
        var nc: NavController? = this
        while (nc != null) {
            if (nc.key == key) return nc
            else nc = nc.parent
        }
        return null
    }

    public fun getCurrentNavEntry(): NavEntry<*>? = currentTransitionFlow.value?.targetEntry

    public fun getBackStack(): List<NavEntry<*>> = internalCurrentBackStackFlow.value

    public fun canGoBack(): Boolean = getBackStack().isNotEmpty()

    public fun editBackStack(actions: BackStackEditScope.() -> Unit) {
        BackStackEditScope(getBackStack())
            .apply(actions)
            .let { updateBackStackInternal(it.backStack) }
    }

    // ----------- public methods --------------------------------------------------------------------------------------

    public fun <Args> navigate(
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

    public fun <Args> replace(
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

    public fun <Args> popToTop(
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

    public fun route(vararg elements: RouteElement): Unit = TODO()

    // todo update tests
    public fun back(
        to: NavDestination<*>? = null,
        result: Any? = null,
        inclusive: Boolean = false,
        transitionData: Any? = null,
        orElse: NavController.() -> Boolean = {
            parent?.back() ?: false
        }
    ): Boolean {
        val backStack = getBackStack()
        val targetIndex =
            if (to != null) backStack
                .indexOfLast { it.destination == to }
                .let { if (inclusive) it - 1 else it }
                .also { if (it < 0) error("Destination not found") }
            else backStack.size - 1
        return if (targetIndex >= 0) {
            val currentNavEntry = getCurrentNavEntry()
            val targetNavEntry = backStack[targetIndex]
            targetNavEntry.navResult = result
            currentNavEntry?.detachFromNavController()
            editBackStack {
                while (backStack.lastIndex != targetIndex) removeLast()
                removeWithoutDetach(targetNavEntry)
            }
            updateCurrentNavEntryInternal(currentNavEntry, targetNavEntry, false, transitionData)
            true
        } else orElse()
    }

    // ----------- internal methods ------------------------------------------------------------------------------------

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

    override fun toString(): String =
        "NavController2(key=$key, current=${getCurrentNavEntry()?.destination?.name}, parent=${parent?.key}}"

    // ----------- support classes -------------------------------------------------------------------------------------

    public data class Transition(
        val targetEntry: NavEntry<*>?,
        val transitionData: Any?,
        val isForward: Boolean,
    )

    // todo add test remove->detach entry, add -> check for not be attached
    public class BackStackEditScope internal constructor(
        initialBackStack: List<NavEntry<*>>
    ) {

        private val internalEditableBackStack = initialBackStack.toMutableList()
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
        public fun <Args> add(
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
        public fun <Args> add(
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
        public fun <Args> add(
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
        public fun <Args> add(
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
            if (index in backStack.indices)
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
         *
         * @param destinations The destinations to set.
         */
        public fun set(vararg destinations: NavDestination<*>) {
            clear()
            destinations.onEach { add(it) }
        }

        /**
         * Sets the back stack to the specified entries.
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