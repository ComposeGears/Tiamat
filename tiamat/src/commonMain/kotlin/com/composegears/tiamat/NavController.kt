package com.composegears.tiamat

import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.*
import com.composegears.tiamat.Route.Companion.resolve

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
     * @return current active NavDestination as State object.
     */
    public var current: NavDestination<*>? by mutableStateOf(null, neverEqualPolicy())
        private set

    /**
     * @return current active NavEntry as State object.
     */
    public var currentNavEntry: NavEntry<*>? by mutableStateOf(null)
        private set

    /**
     * @return true if there is entities in back stack, false otherwise.
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
    internal var transitionController: TransitionController? = null
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
     * Save the current `NavController` state fully, regardless of its `storageMode`,
     * and its children's states based on their `storageMode`.
     *
     * @return The saved state of the `NavController`.
     */
    public fun getSavedState(): SavedState = getFullSavedState()

    /**
     * Load the `NavController` state from the provided saved state.
     *
     * Use with caution, calling this method will reset backstack and current entry.
     *
     * @param savedState The saved state to load from.
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
     * Finds a destination that matches the given predicate.
     *
     * @param predicate A function that takes a `NavDestination` and returns `true` if it matches the criteria.
     * @return The first `NavDestination` that matches the predicate, or `null` if no match is found.
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
     * Finds a `NavController` with the specified key by traversing up the parent hierarchy.
     *
     * @param key The key of the `NavController` to find.
     * @return The `NavController` with the specified key, or `null` if nothing found.
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

    /**
     * Sets the pending back transition.
     *
     * @param transition The content transform for the transition.
     */
    public fun setPendingBackTransition(transition: ContentTransform? = null) {
        this.pendingBackTransition = transition
    }

    /**
     * Gets the back stack.
     *
     * @return The list of navigation entries in the back stack.
     */
    public fun getBackStack(): List<NavEntry<*>> = backStack

    /**
     * Edits the back stack with the provided actions.
     *
     * @param actions The actions to perform on the back stack.
     */
    public fun editBackStack(actions: BackStackEditScope.() -> Unit) {
        BackStackEditScope().actions()
        canGoBack = backStack.isNotEmpty()
    }

    /**
     * Navigates to the specified entry.
     *
     * @param entry The navigation entry to navigate to.
     * @param transition The content transform for the transition.
     * @param transitionController The controller for the transition.
     */
    public fun <Args> navigate(
        entry: NavEntry<Args>,
        transition: ContentTransform? = null,
        transitionController: TransitionController? = null,
    ): Unit = navigateInternal(
        entry = entry,
        transition = transition,
        transitionController = transitionController
    )

    /**
     * Navigates to the specified destination.
     *
     * @param dest The destination to navigate to.
     * @param navArgs The navigation navArgs.
     * @param freeArgs The navigation freeArgs.
     * @param transition The content transform for the transition.
     * @param transitionController The controller for the transition.
     */
    public fun <Args> navigate(
        dest: NavDestination<Args>,
        navArgs: Args? = null,
        freeArgs: Any? = null,
        transition: ContentTransform? = null,
        transitionController: TransitionController? = null,
    ): Unit = navigateInternal(
        entry = NavEntry(dest, navArgs, freeArgs),
        transition = transition,
        transitionController = transitionController
    )

    // internal nav impl
    private fun <Args> navigateInternal(
        entry: NavEntry<Args>,
        transition: ContentTransform? = null,
        transitionController: TransitionController? = null,
    ) {
        requireKnownDestination(entry.destination)
        currentNavEntry?.let { backStack.add(it) }
        replaceInternal(
            entry = entry,
            contentTransition = transition,
            transitionController = transitionController
        )
    }

    /**
     * Replaces the current entry with the specified entry.
     *
     * @param entry The navigation entry to replace with.
     * @param transition The content transform for the transition.
     * @param transitionController The controller for the transition.
     */
    public fun <Args> replace(
        entry: NavEntry<Args>,
        transition: ContentTransform? = null,
        transitionController: TransitionController? = null,
    ): Unit = replace(
        dest = entry.destination,
        navArgs = entry.navArgs,
        freeArgs = entry.freeArgs,
        transition = transition,
        transitionController = transitionController
    )

    /**
     * Replaces the current destination with the specified destination.
     *
     * @param dest The destination to replace with.
     * @param navArgs The navigation navArgs.
     * @param freeArgs The navigation freeArgs.
     * @param transition The content transform for the transition.
     * @param transitionController The controller for the transition.
     */
    public fun <Args> replace(
        dest: NavDestination<Args>,
        navArgs: Args? = null,
        freeArgs: Any? = null,
        transition: ContentTransform? = null,
        transitionController: TransitionController? = null,
    ): Unit = replaceInternal(
        entry = NavEntry(dest, navArgs, freeArgs),
        contentTransition = transition,
        transitionController = transitionController
    )

    // internal replace impl
    private fun replaceInternal(
        entry: NavEntry<*>,
        contentTransition: ContentTransform? = null,
        transitionController: TransitionController? = null,
    ) {
        isForwardTransition = true
        isInitialTransition = currentNavEntry == null
        this.contentTransition = contentTransition
        this.transitionController = transitionController
        setCurrentNavEntryInternal(entry)
    }

    /**
     * Pops the back stack to the top destination.
     *
     * @param dest The destination to pop to.
     * @param transition The content transform for the transition.
     * @param orElse The action to perform if the destination is not found.
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
            replaceInternal(
                entry = entry,
                contentTransition = transition,
                transitionController = null
            )
        } else orElse()
    }

    /**
     * Routes to the specified route.
     *
     * @param route The route to follow.
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
            val upcomingCurrent = pendingEntries.removeAt(pendingEntries.lastIndex)
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
     * Navigates back.
     *
     * @param result The result to pass back.
     * @param transition The content transform for the transition.
     * @param transitionController The controller for the transition.
     * @return `true` if the back navigation was successful, `false` otherwise.
     */
    public fun back(
        result: Any? = null,
        transition: ContentTransform? = pendingBackTransition,
        transitionController: TransitionController? = null,
    ): Boolean = backInternal(
        to = null,
        result = result,
        inclusive = false,
        contentTransition = transition,
        transitionController = transitionController
    )

    /**
     * Navigates back to the specified destination.
     *
     * @param to The destination to navigate back to.
     * @param result The result to pass back.
     * @param inclusive Whether to include the destination in the navigation.
     * @param transition The content transform for the transition.
     * @param transitionController The controller for the transition.
     * @return `true` if the back navigation was successful, `false` otherwise.
     */
    public fun back(
        to: NavDestination<*>,
        result: Any? = null,
        inclusive: Boolean = false,
        transition: ContentTransform? = pendingBackTransition,
        transitionController: TransitionController? = null,
    ): Boolean = backInternal(
        to = to,
        result = result,
        inclusive = inclusive,
        contentTransition = transition,
        transitionController = transitionController
    )

    // internal back impl
    private fun backInternal(
        to: NavDestination<*>? = null,
        result: Any? = null,
        inclusive: Boolean = false,
        contentTransition: ContentTransform? = pendingBackTransition,
        transitionController: TransitionController? = null,
    ): Boolean {
        isForwardTransition = false
        isInitialTransition = currentNavEntry == null
        this.contentTransition = contentTransition
        this.transitionController = transitionController
        if (to != null) {
            while (backStack.isNotEmpty() && backStack.last().destination.name != to.name) {
                backStack.removeAt(backStack.lastIndex).close()
            }
            if (inclusive && backStack.isNotEmpty()) {
                backStack.removeAt(backStack.lastIndex).close()
            }
        }
        return if (backStack.isNotEmpty()) {
            val target = backStack.removeAt(backStack.lastIndex)
            target.navResult = result
            setCurrentNavEntryInternal(target)
            true
        } else {
            parent?.backInternal(to, result, inclusive, contentTransition) ?: false
        }
    }

    // close and cleanup refs
    internal fun close() {
        editBackStack { clear() }
        currentNavEntry?.close()
        sharedViewModels.map { it.value }.onEach { it.close() }
        sharedViewModels.clear()
        setCurrentNavEntryInternal(null)
    }

    public inner class BackStackEditScope internal constructor() {
        /**
         * Adds a navigation entry to the back stack.
         *
         * @param entry The navigation entry to add.
         */
        public fun <Args> add(
            entry: NavEntry<Args>,
        ) {
            add(entry.destination, entry.navArgs, entry.freeArgs)
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
            backStack.add(NavEntry(dest, navArgs, freeArgs))
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
            add(index, entry.destination, entry.navArgs, entry.freeArgs)
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
            backStack.add(index, NavEntry(dest, navArgs, freeArgs))
        }

        /**
         * Removes the navigation entry at the specified index.
         *
         * @param index The index of the entry to remove.
         */
        public fun removeAt(index: Int) {
            if (index in backStack.indices)
                backStack.removeAt(index).close()
        }

        /**
         * Removes the last navigation entry from the back stack.
         *
         * @return `true` if the entry was successfully removed, `false` otherwise.
         */
        public fun removeLast(): Boolean {
            return if (backStack.isNotEmpty()) {
                backStack.removeAt(backStack.lastIndex).close()
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
            if (ind >= 0) backStack.removeAt(ind).close()
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
            if (ind >= 0) backStack.removeAt(ind).close()
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
                    backStack.removeAt(i).close()
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
                backStack.removeAt(backStack.lastIndex).close()
            }
        }

        /**
         * Gets the size of the back stack.
         *
         * @return The size of the back stack.
         */
        public fun size(): Int = backStack.size
    }
}