package com.composegears.tiamat

/**
 * @return true if all elements match the given predicate or collection is empty
 */
internal fun <T> Iterable<T>.allIndexed(predicate: (Int, T) -> Boolean): Boolean {
    if (this is Collection && isEmpty()) return true
    forEachIndexed { index, element ->
        if (!predicate(index, element)) return false
    }
    return true
}