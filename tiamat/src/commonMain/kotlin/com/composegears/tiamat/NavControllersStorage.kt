package com.composegears.tiamat

internal class NavControllersStorage(restoredNavControllers: List<NavController>? = null) {
    private val savedNavControllers = restoredNavControllers?.toMutableList() ?: ArrayList()
    private val activeChildNavControllers = ArrayList<NavController>()

    fun consume() = savedNavControllers.removeFirstOrNull()

    internal fun attachNavController(navController: NavController) {
        activeChildNavControllers.add(navController)
    }

    internal fun detachNavController(navController: NavController) {
        activeChildNavControllers.remove(navController)
    }

    fun save() {
        savedNavControllers.clear()
        savedNavControllers.addAll(activeChildNavControllers)
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