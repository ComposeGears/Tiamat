package com.composegears.tiamat

import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Navigation controller class
 *
 * Provides navigation action
 */
class NavController internal constructor(
    val parent: NavController?,
    val key: String?,
    val storageMode: StorageMode,
    val startDestination: NavDestination<*>?,
    private val savedState: Map<String, Any?>?,
    private val destinations: Array<NavDestination<*>>
) {
    companion object {

        private var nextUUID = 0L

        const val KEY_UUID = "NavController#uuid"
        const val KEY_CURRENT = "NavController#current"
        const val KEY_BACKSTACK = "NavController#backStack"
    }

    // needs to be restored asap not to rent extra uuid's
    private val uuid: Long = (savedState?.get(KEY_UUID) as? Long) ?: nextUUID++

    /**
     * provides current active NavDestination as State object
     */
    var current by mutableStateOf<NavDestination<*>?>(null)
        private set

    /**
     * @return true if there is entities in back stack, false otherwise
     */
    var canGoBack by mutableStateOf(false)
        private set

    private val backStack: ArrayList<NavEntry> = ArrayList()
    private var pendingBackTransition: ContentTransform? = null

    internal var currentNavEntry by mutableStateOf<NavEntry?>(null)
        private set
    internal var dataStorage: DataStorage = DataStorage()
        private set
    internal var isForwardTransition = true
        private set
    internal var isInitialTransition = true
        private set
    internal var contentTransition: ContentTransform? = null
        private set

    init {
        // ensure next uuid will be unique after restoring state of this one
        nextUUID = maxOf(nextUUID, uuid + 1)
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

    private fun setCurrentNavEntry(
        navEntry: NavEntry?,
        closeEntry: Boolean,
    ) {
        if (closeEntry) currentNavEntry?.close()
        currentNavEntry = navEntry
        current = navEntry?.destination
        pendingBackTransition = null
        canGoBack = backStack.isNotEmpty()
    }

    private fun replaceInternal(
        entry: NavEntry,
        closeEntry: Boolean,
        transition: ContentTransform? = null
    ) {
        isForwardTransition = true
        isInitialTransition = currentNavEntry == null
        contentTransition = transition
        setCurrentNavEntry(entry, closeEntry)
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
        if (currentNavEntry != null) backStack.add(currentNavEntry!!.apply { saveState() })
        replaceInternal(NavEntry(dest, dataStorage, navArgs, freeArgs), false, transition)
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
            if (currentNavEntry != null) backStack.add(currentNavEntry!!.apply { saveState() })
            backStack.remove(entry)
            replaceInternal(entry, false, transition)
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
        replaceInternal(NavEntry(dest, dataStorage, navArgs, freeArgs), true, transition)
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
            setCurrentNavEntry(target, true)
            true
        } else {
            parent?.back(result, to, transition) ?: false
        }
    }

    /**
     * If storageMode is Savable - generate full save data,
     * otherwise write the data into data storage and provide minimal restoration info
     * to be saved (mostly nav stack + uuids & keys)
     */
    internal fun saveState(): Map<String, Any?> {
        if (storageMode == StorageMode.Savable) {
            return mapOf(
                KEY_UUID to uuid,
                KEY_CURRENT to currentNavEntry?.apply { saveState() }?.toFullSavedState(),
                KEY_BACKSTACK to backStack.map { it.toFullSavedState() }
            )
        } else {
            dataStorage.data[KEY_CURRENT] = currentNavEntry?.apply { saveState() }?.toFullSavedState()
            dataStorage.data[KEY_BACKSTACK] = backStack.map { it.toFullSavedState() }
            return mapOf(
                KEY_UUID to uuid,
                KEY_CURRENT to currentNavEntry?.apply { saveState() }?.toShortSavedState(),
                KEY_BACKSTACK to backStack.map { it.toShortSavedState() }
            )
        }
    }

    internal fun restoreState(parentDataStorage: DataStorage) {
        val storageKey = "NavController#${key ?: "NoKey"}#$uuid"
        val isStateLost = parentDataStorage.data[storageKey] == null
        dataStorage = parentDataStorage.data.getOrPut(storageKey) { DataStorage() } as DataStorage
        when {
            storageMode == StorageMode.Savable -> restoreFromSavedState(savedState)
            storageMode == StorageMode.IgnoreDataLoss && isStateLost -> restoreFromSavedState(savedState)
            storageMode == StorageMode.ResetOnDataLoss && isStateLost -> reset()
            else -> restoreFromSavedState(dataStorage.data)
        }
    }

    private fun reset() {
        editBackStack { clear() }
        setCurrentNavEntry(null, true)
        if (startDestination != null) navigate(startDestination)
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreFromSavedState(savedState: Map<String, Any?>?) {
        if (savedState != null) runCatching {
            editBackStack { clear() }
            val currentNavEntry = (savedState[KEY_CURRENT] as? Map<String, Any?>?)
                ?.let { NavEntry.restoreNavEntry(it, dataStorage, destinations) }
            (savedState[KEY_BACKSTACK] as List<Map<String, Any?>>)
                .mapTo(backStack) { NavEntry.restoreNavEntry(it, dataStorage, destinations) }
            setCurrentNavEntry(currentNavEntry, true)
        }
        if (currentNavEntry == null && startDestination != null)
            navigate(startDestination)
    }

    internal fun close() {
        editBackStack { clear() }
        setCurrentNavEntry(null, true)
        dataStorage.close()
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
            backStack.add(NavEntry(dest, dataStorage, navArgs, freeArgs))
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
            backStack.add(index, NavEntry(dest, dataStorage, navArgs, freeArgs))
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