package com.composegears.tiamat.navigation

public class NavControllersStorage {
    private companion object {
        private const val KEY_ITEMS = "items"
    }

    private var internalNestedNavControllers: ArrayList<NavController> = ArrayList()
    public val nestedNavControllers: List<NavController> = internalNestedNavControllers

    internal fun saveToSavedState(): SavedState = SavedState(
        KEY_ITEMS to internalNestedNavControllers.filter { it.saveable }.map { it.saveToSavedState() }
    )

    @Suppress("UNCHECKED_CAST")
    internal fun loadFromSavedState(savedState: SavedState?) {
        savedState ?: return
        internalNestedNavControllers = savedState[KEY_ITEMS] as ArrayList<NavController>
    }

    internal fun get(key: String?): NavController? =
        internalNestedNavControllers.firstOrNull { it.key == key }

    internal fun add(navController: NavController) {
        // todo add test only unique nc-s allowed
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