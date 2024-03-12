package com.composegears.tiamat

class NavBackHandler {

    private val backHandlers = ArrayList<() -> Unit>()

    fun back(): Boolean = backHandlers
        .lastOrNull()
        ?.invoke() != null

    fun add(callback: () -> Unit) {
        backHandlers.add(callback)
    }

    fun remove(callback: () -> Unit) {
        backHandlers.remove(callback)
    }
}