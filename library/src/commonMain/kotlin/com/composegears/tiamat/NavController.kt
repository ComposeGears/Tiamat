package com.composegears.tiamat

import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.composegears.tiamat.NavEntry.Companion.restoreNavEntry

/**
 * Navigation controller class
 *
 * Provides navigation action
 */
class NavController internal constructor(
    val parent: NavController?,
    val key: Any?,
    val storageMode: StorageMode,
    val startDestination: NavDestination<*>?,
    internal val dataStorage: DataStorage,
    private val savedState: Map<String, Any?>?,
    private val destinations: Array<NavDestination<*>>
) {
    companion object {
        const val KEY_CURRENT = "current"
        const val KEY_BACKSTACK = "backStack"
    }

    /**
     * provides current active NavDestination as State object
     */
    var current by mutableStateOf<NavDestination<*>?>(null)
        private set

    private val backStack: ArrayList<NavEntry> = ArrayList()
    private var onCloseEntryListener: ((NavEntry) -> Unit)? = null
    private var pendingBackTransition: ContentTransform? = null

    internal var currentNavEntry by mutableStateOf<NavEntry?>(null)
        private set
    internal var isForwardTransition = true
        private set
    internal var isInitialTransition = true
        private set
    internal var contentTransition: ContentTransform? = null
        private set

    init {
        val namesSet = mutableSetOf<String>()
        val duplicates = arrayListOf<String>()
        destinations.onEach {
            if (!namesSet.add(it.name))
                duplicates.add(it.name)
        }
        require(duplicates.isEmpty()) {
            "All destinations should have unique name. Duplicate: $duplicates"
        }
    }

    private fun requireKnownDestination(dest: NavDestination<*>) {
        require(destinations.any { it.name == dest.name }) {
            "${dest.name} is not declared in this nav controller"
        }
    }

    private fun NavEntry.notifyClosed() {
        onCloseEntryListener?.invoke(this)
    }

    private fun setCurrentNavEntry(
        navEntry: NavEntry?,
        notifyClosed: Boolean,
    ) {
        if (notifyClosed) currentNavEntry?.notifyClosed()
        currentNavEntry = navEntry
        current = navEntry?.destination
        pendingBackTransition = null
    }

    private fun replaceInternal(
        entry: NavEntry,
        notifyClosed: Boolean,
        transition: ContentTransform? = null
    ) {
        isForwardTransition = true
        isInitialTransition = currentNavEntry == null
        contentTransition = transition
        setCurrentNavEntry(entry, notifyClosed)
    }

    internal fun setOnCloseEntryListener(listener: ((NavEntry) -> Unit)?) {
        onCloseEntryListener = listener
    }

    /**
     * Set default value for next [back] nav transition
     */
    fun setPendingBackTransition(transition: ContentTransform? = null) {
        this.pendingBackTransition = transition
    }

    /**
     * @return current backstack destinations list
     */
    fun getBackStack() = backStack.map { it.destination }

    /**
     * Place current destination in back stack and open new one
     *
     * @param dest entry to open
     * @param navArgs args to be provided to destination
     * @param transition transition animation
     */
    fun <Args> navigate(
        dest: NavDestination<Args>,
        navArgs: Args? = null,
        transition: ContentTransform? = null
    ) {
        requireKnownDestination(dest)
        if (currentNavEntry != null) backStack.add(currentNavEntry!!.apply { saveState() })
        replaceInternal(NavEntry(dest, navArgs), false, transition)
    }

    /**
     * Place current destination in back stack.
     * If [dest] found in back stack it will be removed from it and opened
     * otherwise it will be created and opened
     *
     * @param dest entry to open
     * @param navArgs args to be provided to destination
     * @param transition transition animation
     */
    fun <Args> popToTop(
        dest: NavDestination<Args>,
        navArgs: Args? = null,
        transition: ContentTransform? = null
    ) {
        if (currentNavEntry?.destination?.name == dest.name) return
        val entry = backStack.find { it.destination.name == dest.name }
        if (entry != null) {
            if (currentNavEntry != null) backStack.add(currentNavEntry!!.apply { saveState() })
            backStack.remove(entry)
            replaceInternal(entry, false, transition)
        } else navigate(dest, navArgs, transition)
    }

    /**
     * Close & remove current destination and open new one
     *
     * @param dest entry to open
     * @param navArgs args to be provided to destination
     * @param transition transition animation
     */
    fun <Args> replace(
        dest: NavDestination<Args>,
        navArgs: Args? = null,
        transition: ContentTransform? = null
    ) {
        replaceInternal(NavEntry(dest, navArgs), true, transition)
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
            backStack.removeLast().notifyClosed()
        }
        return if (backStack.isNotEmpty()) {
            val target = backStack.removeLast()
            target.navResult = result
            setCurrentNavEntry(target, true)
            true
        } else {
            parent?.back(result, to, transition) ?: false
        }
    }

    /**
     * @return true if there is entities in back stack, false otherwise
     */
    fun canGoBack() = backStack.isNotEmpty()

    internal fun reset() {
        backStack.clear()
        setCurrentNavEntry(null, false)
        if (startDestination != null) navigate(startDestination)
    }

    internal fun restoreFromSavedState() {
        savedState?.let(::restoreFromSavedState)
        if (currentNavEntry == null && startDestination != null)
            navigate(startDestination)
    }

    internal fun toSavedState() = currentNavEntry?.let {
        mapOf(
            KEY_CURRENT to currentNavEntry?.apply { saveState() }?.toSavedState(storageMode),
            KEY_BACKSTACK to backStack.map { it.toSavedState(storageMode) }
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreFromSavedState(savedState: Map<String, Any?>) {
        runCatching {
            backStack.clear()
            val currentNavEntry = (savedState[KEY_CURRENT] as Map<String, Any?>)
                .restoreNavEntry(storageMode, destinations)
            (savedState[KEY_BACKSTACK] as List<Map<String, Any?>>)
                .mapTo(backStack) { it.restoreNavEntry(storageMode, destinations) }
            setCurrentNavEntry(currentNavEntry, false)
        }
    }
}