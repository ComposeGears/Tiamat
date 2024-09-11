package com.composegears.tiamat

public class NavBackHandler {

    private val backHandlers = ArrayList<() -> Unit>()

    public fun back(): Boolean = backHandlers
        .lastOrNull()
        ?.invoke() != null

    internal fun add(callback: () -> Unit) {
        backHandlers.add(callback)
    }

    internal fun remove(callback: () -> Unit) {
        backHandlers.remove(callback)
    }
}