package com.composegears.tiamat.navigation

internal class NavControllersStorage {
    private companion object {
        private const val KEY_ITEMS = "items"
    }

    private val internalNestedNavControllers: ArrayList<NavController> = ArrayList()
    internal val nestedNavControllers: List<NavController> = internalNestedNavControllers

    internal fun saveToSavedState(): SavedState = SavedState(
        KEY_ITEMS to internalNestedNavControllers.filter { it.saveable }.map { it.saveToSavedState() }
    )

    @Suppress("UNCHECKED_CAST")
    internal fun loadFromSavedState(parent: NavController?, savedState: SavedState?) {
        clear()
        savedState ?: return
        (savedState[KEY_ITEMS] as Iterable<SavedState>)
            .map { NavController.restoreFromSavedState(parent, it) }
            .let { internalNestedNavControllers.addAll(it) }
    }

    internal fun get(key: String?): NavController? =
        internalNestedNavControllers.firstOrNull { it.key == key }

    internal fun add(navController: NavController) {
        if (get(navController.key) != null) error(
            "NavController with key ${navController.key} already exists"
        )
        internalNestedNavControllers.add(navController)
    }

    internal fun remove(navController: NavController) {
        internalNestedNavControllers.remove(navController)
    }

    internal fun clear() {
        internalNestedNavControllers.onEach { it.close() }
        internalNestedNavControllers.clear()
    }
}