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
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This is an experimental Tiamat API, and it is likely to be changed in the future."
)
public annotation class TiamatExperimentalApi