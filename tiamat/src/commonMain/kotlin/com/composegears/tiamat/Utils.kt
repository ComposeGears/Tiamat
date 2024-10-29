package com.composegears.tiamat

/**
 * SaveState declaration alias
 */
public typealias SavedState = Map<String, Any?>

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

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING, message = "Under development feature")
public annotation class TiamatExperimentalApi