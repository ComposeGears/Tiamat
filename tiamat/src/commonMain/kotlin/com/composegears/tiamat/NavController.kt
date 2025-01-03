package com.composegears.tiamat

import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.*
import com.composegears.tiamat.Route.Companion.resolve

/**
 * Navigation controller class
 *
 * Provides navigation action
 */
@Stable
public class NavController internal constructor(
    public val key: String?,
    public val parent: NavController?,
    internal val storageMode: StorageMode,
    internal val canBeSaved: (Any) -> Boolean,
    internal val startDestination: NavEntry<*>?,
    private val destinations: Array<NavDestination<*>>,
    savedState: SavedState?
) {

    public companion object {
        private const val KEY_KEY = "key"
        private const val KEY_STORAGE_MODE = "storageMode"
        private const val KEY_PENDING_ENTRY_NAV_ID = "pendingEntryNavId"
        private const val KEY_START_DESTINATION = "startDestination"
        private const val KEY_DESTINATIONS = "destinations"
        private const val KEY_CURRENT = "current"
        private const val KEY_BACKSTACK = "backStack"

        private const val DESTINATIONS_JOIN_SEPARATOR = ", "

        internal fun isSame(
            navController: NavController,
            key: String?,
            storageMode: StorageMode,
            startDestination: NavEntry<*>?,
            destinations: Array<NavDestination<*>>,
        ) = navController.key == key &&
            navController.storageMode == storageMode &&
            navController.startDestination?.destination == startDestination?.destination &&
            navController.destinations.contentEquals(destinations)

        internal fun isSame(
            savedState: SavedState,
            key: String?,
            storageMode: StorageMode,
            startDestination: NavEntry<*>?,
            destinations: Array<NavDestination<*>>,
        ) = savedState[KEY_KEY] == key &&
            savedState[KEY_STORAGE_MODE] == storageMode.name &&
            savedState[KEY_START_DESTINATION] == startDestination?.destination?.name &&
            savedState[KEY_DESTINATIONS]
                .let { it as String }
                .split(DESTINATIONS_JOIN_SEPARATOR)
                .allIndexed { index, destination -> destinations[index].name == destination }
    }

    /**
     * provides current active NavDestination as State object
     */
    public var current: NavDestination<*>? by mutableStateOf(null, neverEqualPolicy())
        private set

    /**
     * provides current active NavEntry as State object
     */
    public var currentNavEntry: NavEntry<*>? by mutableStateOf(null)
        private set

    /**
     * @return true if there is entities in back stack, false otherwise
     */
    public var canGoBack: Boolean by mutableStateOf(false)
        private set

    private val backStack: ArrayList<NavEntry<*>> = ArrayList()
    internal val sharedViewModels = mutableMapOf<String, TiamatViewModel>()
    private var pendingBackTransition: ContentTransform? = null
    private var pendingRoute: Route? = null

    internal var isForwardTransition = true
        private set
    internal var isInitialTransition = true
        private set
    internal var contentTransition: ContentTransform? = null
        private set

    private var pendingEntryNavId = 0L

    init {
        // ensure there is no same-named destinations
        val namesSet = mutableSetOf<String>()
        val duplicates = arrayListOf<String>()
        destinations.onEach {
            if (!namesSet.add(it.name))
                duplicates.add(it.name)
        }
        require(duplicates.isEmpty()) {
            "All destinations should have unique name. Duplicate: $duplicates"
        }
        // ensure start destination is known
        if (startDestination != null)
            requireKnownDestination(startDestination.destination)
        // load from saved state
        if (!savedState.isNullOrEmpty()) runCatching {
            restoreFromSavedState(savedState)
        }
        // go to start destination if nothing restored
        if (currentNavEntry == null && backStack.isEmpty() && startDestination != null)
            setCurrentNavEntryInternal(NavEntry(startDestination))
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreFromSavedState(savedState: SavedState) {
        pendingEntryNavId = savedState[KEY_PENDING_ENTRY_NAV_ID] as Long
        val currentNavEntry = (savedState[KEY_CURRENT] as? SavedState?)
            ?.let { NavEntry.restore(it, destinations) }
        (savedState[KEY_BACKSTACK] as List<SavedState>)
            .mapTo(backStack) { NavEntry.restore(it, destinations) }
        setCurrentNavEntryInternal(currentNavEntry)
    }

    private fun getMinimalVerificationSavedState() = mapOf(
        KEY_KEY to key,
        KEY_STORAGE_MODE to storageMode.name,
        KEY_START_DESTINATION to startDestination?.destination?.name,
        KEY_DESTINATIONS to destinations.joinToString(DESTINATIONS_JOIN_SEPARATOR) { it.name },
    )

    private fun <T> NavEntry<T>.checkIfSaveable() = apply {
        fun test(data: Any, kind: String) {
            if (!canBeSaved(data)) error(
                "Unable to save ${destination.name}\n" +
                    "$kind of type ${data::class.simpleName} is not saveable for current (key=$key) NavController"
            )
        }
        navArgs?.let { test(it, "navArgs") }
        freeArgs?.let { test(it, "freeArgs") }
        navResult?.let { test(it, "navResult") }
    }

    private fun getFullSavedState() = getMinimalVerificationSavedState() + mapOf(
        KEY_PENDING_ENTRY_NAV_ID to pendingEntryNavId,
        KEY_CURRENT to currentNavEntry?.checkIfSaveable()?.saveToSaveState(),
        KEY_BACKSTACK to backStack.map { it.checkIfSaveable().saveToSaveState() }
    )

    internal fun saveToSaveState(): SavedState = when (storageMode) {
        StorageMode.SavedState -> getFullSavedState()
        StorageMode.Memory -> getMinimalVerificationSavedState()
    }

    /**
     * Save current navController state(full, regardless of `storageMode`)
     * and it's children states(depend on theirs `storageMode`)
     *
     * @return saved state
     */
    public fun getSavedState(): SavedState = getFullSavedState()

    /**
     * Load navController (and it's children) state from saved state
     *
     * Use with caution, calling this method will reset backstack and current entry
     *
     * @param savedState saved state
     */
    public fun loadFromSavedState(savedState: SavedState) {
        // clear current state
        close()
        // load from saved state
        restoreFromSavedState(savedState)
        // navigate to start destination if nothing restored
        if (currentNavEntry == null && backStack.isEmpty() && startDestination != null)
            setCurrentNavEntryInternal(NavEntry(startDestination))
    }

    /**
     * Search for specific destination from list of known destinations
     *
     * @param predicate the predicate to match a destinations
     *
     * @return the first destination matching [predicate] or null
     */
    public fun findDestination(predicate: (NavDestination<*>) -> Boolean): NavDestination<*>? =
        destinations.find(predicate)

    /**
     * Checks if the given destination is known to the current NavController
     *
     * @param dest The destination to check.
     * @return true if the destination is known, false otherwise
     */
    public fun isKnownDestination(dest: NavDestination<*>): Boolean = destinations.any { it.name == dest.name }

    /**
     * @param key nav controller's key to search for
     *
     * @return NavController instance with same key (current or one of parents), null if no one match
     */
    public fun findNavController(key: String): NavController? {
        var nc: NavController? = this
        while (nc != null) {
            if (nc.key == key) return nc
            else nc = nc.parent
        }
        return null
    }

    private fun requireKnownDestination(dest: NavDestination<*>) {
        require(isKnownDestination(dest)) {
            "${dest.name} is not declared in the current (key = $key) nav controller"
        }
    }

    private fun setCurrentNavEntryInternal(
        navEntry: NavEntry<*>?,
    ) {
        if (navEntry != null && navEntry.navId < 0) navEntry.navId = pendingEntryNavId++
        currentNavEntry = navEntry
        current = navEntry?.destination
        pendingBackTransition = null
        canGoBack = backStack.isNotEmpty()
    }

    private fun replaceInternal(
        entry: NavEntry<*>,
        transition: ContentTransform? = null
    ) {
        isForwardTransition = true
        isInitialTransition = currentNavEntry == null
        contentTransition = transition
        setCurrentNavEntryInternal(entry)
    }

    /**
     * Set default value for next [back] nav transition
     */
    public fun setPendingBackTransition(transition: ContentTransform? = null) {
        this.pendingBackTransition = transition
    }

    /**
     * @return current backstack
     *
     * @see [NavEntry]
     */
    public fun getBackStack(): List<NavEntry<*>> = backStack

    /**
     * Edit current back stack
     */
    public fun editBackStack(actions: BackStackEditScope.() -> Unit) {
        BackStackEditScope().actions()
        canGoBack = backStack.isNotEmpty()
    }

    /**
     * Place current destination in back stack and open copy of entry
     *
     * @param entry entry to open
     * @param transition transition animation
     */
    public fun <Args> navigate(
        entry: NavEntry<Args>,
        transition: ContentTransform? = null
    ) {
        navigate(entry.destination, entry.navArgs, entry.freeArgs, transition)
    }

    /**
     * Place current destination in back stack and open new one
     *
     * @param dest destination to open
     * @param navArgs args to be provided to destination
     * @param freeArgs free args to be provided to destination
     * @param transition transition animation
     */
    public fun <Args> navigate(
        dest: NavDestination<Args>,
        navArgs: Args? = null,
        freeArgs: Any? = null,
        transition: ContentTransform? = null
    ) {
        requireKnownDestination(dest)
        currentNavEntry?.let { backStack.add(it) }
        replaceInternal(NavEntry(dest, navArgs, freeArgs), transition)
    }

    /**
     * Place current destination in back stack.
     * If [dest] found in back stack it will be removed from it and opened
     * otherwise it will be created and opened
     *
     * @param dest destination to open
     * @param transition transition animation
     * @param orElse an action to be taken if destination not found in backstack,
     *               default is to navigate to destination
     */
    public fun <Args> popToTop(
        dest: NavDestination<Args>,
        transition: ContentTransform? = null,
        orElse: NavController.() -> Unit = {
            navigate(dest, null, null, transition)
        }
    ) {
        if (currentNavEntry?.destination?.name == dest.name) return
        val entry = backStack.find { it.destination.name == dest.name }
        if (entry != null) {
            currentNavEntry?.let { backStack.add(it) }
            backStack.remove(entry)
            replaceInternal(entry, transition)
        } else orElse()
    }

    /**
     * Close & remove current destination and open copy of entry
     *
     * @param entry entry to open
     * @param transition transition animation
     */
    public fun <Args> replace(
        entry: NavEntry<Args>,
        transition: ContentTransform? = null
    ) {
        replace(entry.destination, entry.navArgs, entry.freeArgs, transition)
    }

    /**
     * Close & remove current destination and open new one
     *
     * @param dest destination to open
     * @param navArgs args to be provided to destination
     * @param freeArgs free args to be provided to destination
     * @param transition transition animation
     */
    public fun <Args> replace(
        dest: NavDestination<Args>,
        navArgs: Args? = null,
        freeArgs: Any? = null,
        transition: ContentTransform? = null
    ) {
        replaceInternal(NavEntry(dest, navArgs, freeArgs), transition)
    }

    /**
     * Navigate through provided rout
     *
     * @param route route to navigate by
     */
    @TiamatExperimentalApi
    public fun route(route: Route) {
        pendingRoute = route.clone()
        followRoute()
    }

    /**
     * Navigate through parent's rout
     */
    internal fun followParentsRoute() {
        val parentRoute = parent?.pendingRoute ?: return
        val parentElement = parentRoute.elements.firstOrNull()
        val entries = parentElement?.resolve(this)
        if (entries != null) {
            parent.pendingRoute = null
            if (pendingRoute != null) {
                // nc already had to follow some route, but parent wants to follow another
                // skip parent's route and cancel it
                return
            }
            // follow parent's route
            pendingRoute = parentRoute.clone(drop = 1)
            followRoute(entries)
        }
    }

    /**
     * Follow active/parents route or pass it to next NavController
     */
    @Suppress("CyclomaticComplexMethod", "CognitiveComplexMethod", "NestedBlockDepth")
    private fun followRoute(initialEntries: List<NavEntry<*>> = emptyList()) {
        // no route to go -> no actions -> exit
        var pendingRoute = pendingRoute ?: return

        // entries to open
        val pendingEntries = ArrayList(initialEntries)

        // resolve entries from pending route
        var resolvedCount = 0
        while (resolvedCount <= pendingRoute.elements.lastIndex) {
            val entries = pendingRoute.elements[resolvedCount].resolve(this)
            if (entries != null) {
                resolvedCount++
                pendingEntries.addAll(entries)
            } else break
        }
        if (resolvedCount != 0) {
            pendingRoute = pendingRoute.clone(drop = resolvedCount)
            this.pendingRoute = pendingRoute
        }

        // route helper
        fun isSameEntry(old: NavEntry<*>?, new: NavEntry<*>) =
            old != null &&
                old.destination == new.destination &&
                old.navArgs == new.navArgs &&
                old.freeArgs == new.freeArgs &&
                old.navResult == new.navResult

        // apply entries
        var isCurrentDestinationChanged = false
        if (pendingEntries.isNotEmpty()) {
            // last entry should be shown as `new` current
            val upcomingCurrent = pendingEntries.removeLast()
            // calculate how many entries we can skip
            var skipCount = 0
            if (!pendingRoute.forceReplace) {
                for (i in 0..pendingEntries.lastIndex) {
                    if (isSameEntry(backStack.getOrNull(i), pendingEntries[i])) {
                        skipCount++
                    } else break
                }
            }
            // apply backstack changes
            editBackStack {
                if (skipCount == 0) clear()
                else while (size() > skipCount) {
                    removeLast()
                }
                pendingEntries.drop(skipCount).onEach {
                    add(it)
                }
            }
            // apply current destination
            if (pendingRoute.forceReplace || !isSameEntry(currentNavEntry, upcomingCurrent)) {
                replace(upcomingCurrent)
                isCurrentDestinationChanged = true
            }
        }

        // invalidate route if there is no more elements
        if (pendingRoute.elements.isEmpty()) {
            this.pendingRoute = null
            return
        }

        // in case current destination was not changed, we need to find a child to follow rute
        if (!isCurrentDestinationChanged) {
            val nextElement = pendingRoute.elements.first()
            currentNavEntry
                ?.navControllersStorage
                ?.getActiveNavControllers()
                ?.find { nc ->
                    val entries = nextElement.resolve(nc)
                    if (entries != null) {
                        nc.pendingRoute = pendingRoute.clone(drop = 1)
                        this.pendingRoute = null
                        nc.followRoute(entries)
                        true
                    } else false
                }
                ?: invalidateRoute()
        }
    }

    /**
     * We need ether close unfinished rout or else indicate path error
     *
     * Should be called after child destination being instantiated
     */
    internal fun invalidateRoute() {
        if (pendingRoute?.throwOnFail == true) error(
            listOfNotNull(
                "Route not finished:",
                key?.let { "\tNavController: $it" },
                current?.name?.let { "Destination: $it" },
                pendingRoute?.elements?.firstOrNull()?.let {
                    when (it) {
                        is NavDestination<*> -> "Unable to find destination: ${it.name}"
                        is NavEntry<*> -> "Unable to open entry: ${it.destination.name}"
                        is Route.RouteEntries -> "Unable to find route destination (${it.description})"
                        is Route.RouteNavController -> "Unable to find nav controller (${it.description})"
                    }
                }
            ).joinToString("\n")
        )
        pendingRoute = null
    }

    /**
     * Close current destination. Navigate to previous destination from backstack.
     * If there is no entities in backstack, action will be redirected to parent navController
     *
     * @param result data to be provided as result
     * @param transition transition animation
     *
     * @return true if navigation successful, otherwise false
     */
    public fun back(
        result: Any? = null,
        transition: ContentTransform? = pendingBackTransition
    ): Boolean = backInternal(null, result, false, transition)

    /**
     * Close current destination. Navigate to previous destination from backstack.
     * If there is no entities in backstack, action will be redirected to parent navController
     *
     * @param to destination to back to
     * @param result data to be provided as result
     * @param inclusive indicate if target screen should be popped out
     * @param transition transition animation
     *
     * @return true if navigation successful, otherwise false
     */
    public fun back(
        to: NavDestination<*>,
        result: Any? = null,
        inclusive: Boolean = false,
        transition: ContentTransform? = pendingBackTransition
    ): Boolean = backInternal(to, result, inclusive, transition)

    private fun backInternal(
        to: NavDestination<*>? = null,
        result: Any? = null,
        inclusive: Boolean = false,
        transition: ContentTransform? = pendingBackTransition
    ): Boolean {
        isForwardTransition = false
        isInitialTransition = currentNavEntry == null
        contentTransition = transition
        if (to != null) {
            while (backStack.isNotEmpty() && backStack.last().destination.name != to.name) {
                backStack.removeLast().close()
            }
            if (inclusive && backStack.isNotEmpty()) {
                backStack.removeLast().close()
            }
        }
        return if (backStack.isNotEmpty()) {
            val target = backStack.removeLast()
            target.navResult = result
            setCurrentNavEntryInternal(target)
            true
        } else {
            parent?.backInternal(to, result, inclusive, transition) ?: false
        }
    }

    internal fun close() {
        editBackStack { clear() }
        currentNavEntry?.close()
        sharedViewModels.map { it.value }.onEach { it.close() }
        sharedViewModels.clear()
        setCurrentNavEntryInternal(null)
    }

    public inner class BackStackEditScope internal constructor() {
        /**
         * Add a copy of entry into backstack
         *
         * @param entry backstack entry
         */
        public fun <Args> add(
            entry: NavEntry<Args>,
        ) {
            add(entry.destination, entry.navArgs, entry.freeArgs)
        }

        /**
         * Add destination into backstack
         *
         * @param dest backstack destination
         * @param navArgs args to be provided to destination
         * @param freeArgs free args to be provided to destination
         */
        public fun <Args> add(
            dest: NavDestination<Args>,
            navArgs: Args? = null,
            freeArgs: Any? = null,
        ) {
            backStack.add(NavEntry(dest, navArgs, freeArgs))
        }

        /**
         * Add a copy of entry into backstack at specific position
         *
         * @param index position
         * @param entry backstack entry
         */
        public fun <Args> add(
            index: Int,
            entry: NavEntry<Args>,
        ) {
            add(index, entry.destination, entry.navArgs, entry.freeArgs)
        }

        /**
         * Add destination into backstack at specific position
         *
         * @param index position
         * @param dest backstack destination
         * @param navArgs args to be provided to destination
         * @param freeArgs free args to be provided to destination
         */
        public fun <Args> add(
            index: Int,
            dest: NavDestination<Args>,
            navArgs: Args? = null,
            freeArgs: Any? = null,
        ) {
            backStack.add(index, NavEntry(dest, navArgs, freeArgs))
        }

        /**
         * Remove backstack entry at specific position
         *
         * @param index position
         */
        public fun removeAt(index: Int) {
            if (index in backStack.indices)
                backStack.removeAt(index).close()
        }

        /**
         * Remove latest/most recent item
         *
         * @return true if entry where removed, false otherwise
         */
        public fun removeLast(): Boolean {
            return if (backStack.isNotEmpty()) {
                backStack.removeLast().close()
                true
            } else false
        }

        /**
         * Remove latest/most recent item within same destination
         *
         * @param dest destination to be removed
         * @return true if entry where removed, false otherwise
         */
        public fun removeLast(dest: NavDestination<*>): Boolean {
            val ind = backStack.indexOfLast { it.destination.name == dest.name }
            if (ind >= 0) backStack.removeAt(ind).close()
            return ind >= 0
        }

        /**
         * Remove one latest/most recent item matching predicate
         *
         * @param predicate matcher, default `{true}`
         * @return true if entry where removed, false otherwise
         */
        public fun removeLast(predicate: (NavEntry<*>) -> Boolean): Boolean {
            val ind = backStack.indexOfLast(predicate)
            if (ind >= 0) backStack.removeAt(ind).close()
            return ind >= 0
        }

        /**
         * Remove all items matching predicate
         *
         * @param predicate matcher
         */
        public fun removeAll(predicate: (NavEntry<*>) -> Boolean) {
            var i = 0
            while (i < backStack.size) {
                if (predicate(backStack[i])) {
                    backStack.removeAt(i).close()
                } else i++
            }
        }

        /**
         * Replace the current backstack with the provided destinations
         *
         * @param destinations the destinations to replace the current backstack with
         */
        public fun set(vararg destinations: NavDestination<*>) {
            clear()
            destinations.onEach { add(it) }
        }

        /**
         * Replace the current backstack with the provided entries
         *
         * @param entries the entries to replace the current backstack with
         */
        public fun set(vararg entries: NavEntry<*>) {
            clear()
            entries.onEach { add(it) }
        }

        /**
         * Clear backstack
         */
        public fun clear() {
            while (backStack.isNotEmpty()) {
                backStack.removeLast().close()
            }
        }

        /**
         * @return current backstack items count
         */
        public fun size(): Int = backStack.size
    }
}