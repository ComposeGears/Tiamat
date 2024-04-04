package com.composegears.tiamat

internal class NavControllersStorage {

    private val activeChildNavControllers = ArrayList<NavController>()
    private val savedNavControllers = ArrayList<NavController>()
    private var savedState = mutableListOf<Map<String, Any?>>()

    fun consume() = savedNavControllers.removeFirstOrNull()

    fun consumeFromSavedState() = savedState.removeFirstOrNull()

    fun attachNavController(navController: NavController) {
        activeChildNavControllers.add(navController)
    }

    internal fun detachNavController(navController: NavController) {
        activeChildNavControllers.remove(navController)
    }

    fun save() {
        savedState.clear()
        savedNavControllers.clear()
        savedNavControllers.addAll(activeChildNavControllers)
    }

    fun saveToSaveState(): List<Map<String, Any?>> {
        save()
        return activeChildNavControllers.map { it.saveToSaveState() }
    }

    fun restoreFromSavedState(savedState: List<Map<String, Any?>>?) {
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