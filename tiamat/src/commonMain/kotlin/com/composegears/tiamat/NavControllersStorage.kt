package com.composegears.tiamat

/**
 * Nav controllers storage
 *
 * Used to store and track active navControllers, perform create/restore
 */
internal class NavControllersStorage {

    private val activeChildNavControllers = ArrayList<NavController>()
    private val savedNavControllers = ArrayList<NavController>()
    private var savedState = mutableListOf<SavedState>()

    fun restoreOrCreate(
        key: String?,
        parent: NavController?,
        storageMode: StorageMode,
        startDestination: NavEntry<*>?,
        destinations: Array<NavDestination<*>>,
    ): NavController {
        val restoredNavController = savedNavControllers.removeFirstOrNull()
        val restoredSavedState = savedState.removeFirstOrNull()
        var isCorrupted = false
        if (restoredNavController != null) {
            // validate instance
            val isSame = NavController.isSame(
                navController = restoredNavController,
                key = key,
                storageMode = storageMode,
                startDestination = startDestination,
                destinations = destinations
            )
            // restore from instance
            if (isSame) return restoredNavController
            else isCorrupted = true
        } else if (restoredSavedState != null) {
            // validate saved state
            val isSame = NavController.isSame(
                savedState = restoredSavedState,
                key = key,
                storageMode = storageMode,
                startDestination = startDestination,
                destinations = destinations
            )
            // restore from saved state
            if (isSame) return NavController(
                key = key,
                parent = parent,
                storageMode = storageMode,
                startDestination = startDestination,
                destinations = destinations,
                savedState = restoredSavedState
            )
            else isCorrupted = true
        }
        // navController is not matching, we need to clear all saved data
        // we can't give guaranties that others screens are in same order
        if (isCorrupted) {
            savedNavControllers.onEach { it.close() }
            savedNavControllers.clear()
            savedState.clear()
        }
        // create new nav controller
        return NavController(
            key = key,
            parent = parent,
            storageMode = storageMode,
            startDestination = startDestination,
            destinations = destinations,
            savedState = null
        )
    }

    fun attachNavController(navController: NavController) {
        activeChildNavControllers.add(navController)
    }

    fun detachNavController(navController: NavController) {
        activeChildNavControllers.remove(navController)
    }

    fun saveState() {
        savedState.clear()
        savedNavControllers.clear()
        savedNavControllers.addAll(activeChildNavControllers)
    }

    fun saveToSaveState(): List<SavedState> {
        return (activeChildNavControllers + savedNavControllers)
            .toSet()
            .map { it.saveToSaveState() } + savedState
    }

    fun restoreFromSavedState(savedState: List<SavedState>?) {
        this.savedState = savedState?.toMutableList() ?: mutableListOf()
    }

    fun isSaved(navController: NavController) = savedNavControllers.contains(navController)

    fun close() {
        savedNavControllers.onEach {
            activeChildNavControllers.remove(it)
            it.close()
        }
        savedNavControllers.clear()
        activeChildNavControllers.onEach { it.close() }
        activeChildNavControllers.clear()
    }
}