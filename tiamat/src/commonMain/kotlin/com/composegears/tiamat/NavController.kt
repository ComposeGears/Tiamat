@file:Suppress("UNCHECKED_CAST")

package com.composegears.tiamat

import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.*

/**
 * Navigation controller class
 *
 * Provides navigation action
 */
@Stable
class NavController internal constructor(
    val key: String?,
    val parent: NavController?,
    internal val storageMode: StorageMode,
    internal val startDestination: NavEntry<*>?,
    private val destinations: Array<NavDestination<*>>,
    savedState: Map<String, Any?>?
) {

    companion object {
        const val KEY_CURRENT = "current"
        const val KEY_BACKSTACK = "backStack"
    }

    /**
     * provides current active NavDestination as State object
     */
    var current by mutableStateOf<NavDestination<*>?>(null, neverEqualPolicy())
        private set

    /**
     * @return true if there is entities in back stack, false otherwise
     */
    var canGoBack by mutableStateOf(false)
        private set

    private val backStack: ArrayList<NavEntry<*>> = ArrayList()
    private var pendingBackTransition: ContentTransform? = null

    internal var currentNavEntry by mutableStateOf<NavEntry<*>?>(null)
        private set
    internal var isForwardTransition = true
        private set
    internal var isInitialTransition = true
        private set
    internal var contentTransition: ContentTransform? = null
        private set

    private var nextEntryNavId = 0L

    init {
        // ensure there is not same-named destinations
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
        if (savedState != null) runCatching {
            val currentNavEntry = (savedState[KEY_CURRENT] as? Map<String, Any?>?)
                ?.let { NavEntry.restore(it, destinations) }
            (savedState[KEY_BACKSTACK] as List<Map<String, Any?>>)
                .mapTo(backStack) { NavEntry.restore(it, destinations) }
            setCurrentNavEntryInternal(currentNavEntry)
        }
        // go to start destination if nothing restored
        if (currentNavEntry == null && backStack.isEmpty() && startDestination != null)
            setCurrentNavEntryInternal(NavEntry(startDestination))
    }

    internal fun match(
        key: String?,
        parent: NavController?,
        storageMode: StorageMode,
        startDestination: NavEntry<*>?,
        destinations: Array<NavDestination<*>>,
    ) = this.key == key
        && this.parent == parent
        && this.storageMode == storageMode
        && this.startDestination?.destination == startDestination?.destination
        && this.startDestination?.navArgs == startDestination?.navArgs
        && this.startDestination?.freeArgs == startDestination?.freeArgs
        && this.startDestination?.navResult == startDestination?.navResult
        && this.destinations.contentEquals(destinations)

    internal fun saveToSaveState(): Map<String, Any?> = mapOf(
        KEY_CURRENT to currentNavEntry?.saveToSaveState(),
        KEY_BACKSTACK to backStack.map { it.saveToSaveState() }
    )

    /**
     * @param key nav controller's key to search for
     *
     * @return NavController instance with same key (current or one of parents), null if no one match
     */
    fun findNavController(key: String): NavController? {
        var nc: NavController? = this
        while (nc != null) {
            if (nc.key == key) return nc
            else nc = nc.parent
        }
        return null
    }

    private fun requireKnownDestination(dest: NavDestination<*>) {
        require(destinations.any { it.name == dest.name }) {
            "${dest.name} is not declared in this nav controller"
        }
    }

    private fun setCurrentNavEntryInternal(
        navEntry: NavEntry<*>?,
    ) {
        if (navEntry != null && navEntry.navId < 0) navEntry.navId = nextEntryNavId++
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
    fun setPendingBackTransition(transition: ContentTransform? = null) {
        this.pendingBackTransition = transition
    }

    /**
     * @return current backstack
     *
     * @see [NavEntry]
     */
    fun getBackStack() = backStack as List<NavEntry<*>>

    /**
     * Edit current back stack
     */
    fun editBackStack(actions: BackStackEditScope.() -> Unit) {
        BackStackEditScope().actions()
        canGoBack = backStack.isNotEmpty()
    }

    /**
     * Place current destination in back stack and open new one
     *
     * @param dest entry to open
     * @param navArgs args to be provided to destination
     * @param freeArgs free args to be provided to destination
     * @param transition transition animation
     */
    fun <Args> navigate(
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
     * @param dest entry to open
     * @param navArgs args to be provided to destination
     * @param freeArgs free args to be provided to destination
     * @param transition transition animation
     */
    fun <Args> popToTop(
        dest: NavDestination<Args>,
        navArgs: Args? = null,
        freeArgs: Any? = null,
        transition: ContentTransform? = null
    ) {
        if (currentNavEntry?.destination?.name == dest.name) return
        val entry = backStack.find { it.destination.name == dest.name }
        if (entry != null) {
            currentNavEntry?.let { backStack.add(it) }
            backStack.remove(entry)
            replaceInternal(entry, transition)
        } else navigate(dest, navArgs, freeArgs, transition)
    }

    /**
     * Close & remove current destination and open new one
     *
     * @param dest entry to open
     * @param navArgs args to be provided to destination
     * @param freeArgs free args to be provided to destination
     * @param transition transition animation
     */
    fun <Args> replace(
        dest: NavDestination<Args>,
        navArgs: Args? = null,
        freeArgs: Any? = null,
        transition: ContentTransform? = null
    ) {
        replaceInternal(NavEntry(dest, navArgs, freeArgs), transition)
    }

    /**
     * Close current destination. Navigate to previous destination from backstack.
     * If there is no entities in backstack, action will be redirected to parent navController
     *
     * @param result data to be provided to opened entity
     * @param to destination to be searched in backstack or null
     * @param transition transition animation
     */
    fun back(
        result: Any? = null,
        to: NavDestination<*>? = null,
        transition: ContentTransform? = pendingBackTransition
    ): Boolean {
        isForwardTransition = false
        isInitialTransition = currentNavEntry == null
        contentTransition = transition
        if (to != null) while (backStack.isNotEmpty() && backStack.last().destination != to) {
            backStack.removeLast().close()
        }
        return if (backStack.isNotEmpty()) {
            val target = backStack.removeLast()
            target.navResult = result
            setCurrentNavEntryInternal(target)
            true
        } else {
            parent?.back(result, to, transition) ?: false
        }
    }

    internal fun close() {
        editBackStack { clear() }
        currentNavEntry?.close()
        setCurrentNavEntryInternal(null)
    }

    inner class BackStackEditScope internal constructor() {

        /**
         * Add destination into backstack
         *
         * @param dest entry to open
         * @param navArgs args to be provided to destination
         * @param freeArgs free args to be provided to destination
         */
        fun <Args> add(
            dest: NavDestination<Args>,
            navArgs: Args? = null,
            freeArgs: Any? = null,
        ) {
            backStack.add(NavEntry(dest, navArgs, freeArgs))
        }

        /**
         * Add destination into backstack at specific position
         *
         * @param index position
         * @param dest entry to open
         * @param navArgs args to be provided to destination
         * @param freeArgs free args to be provided to destination
         */
        fun <Args> add(
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
        fun removeAt(index: Int) {
            backStack.removeAt(index).close()
        }

        /**
         * Clear backstack
         */
        fun clear() {
            while (backStack.isNotEmpty()) {
                backStack.removeLast().close()
            }
        }
    }
}