package com.composegears.tiamat

/**
 * SaveState declaration alias.
 */
public typealias SavedState = Map<String, Any?>

/**
 * Converts the `SavedState` to a human-readable string representation.
 *
 * This function formats the `SavedState` map into a string representation that is easier to read.
 *
 * @param tabChar The character(s) used for indentation. Defaults to four spaces.
 * @param initialTabSize The initial size of the indentation. Defaults to 0.
 * @return A human-readable string representation of the `SavedState`.
 */
public fun SavedState.toHumanReadableString(
    tabChar: String = "    ",
    initialTabSize: Int = 0,
): String = StringBuilder()
    .apply { appendSavedStateDataString(tabChar, "Data", this@toHumanReadableString, initialTabSize) }
    .toString()

private fun StringBuilder.appendSavedStateDataString(tabChar: String, key: String, data: Any?, tab: Int) {
    val prefix = tabChar.repeat(tab)
    when (data) {
        is Map<*, *> -> {
            append(prefix).append(key)
            if (data.isNotEmpty()) {
                append(" = {\n")
                data.onEach {
                    appendSavedStateDataString(tabChar, it.key.toString(), it.value, tab + 1)
                }
                append(prefix).append("}\n")
            } else {
                append(" = {}\n")
            }
        }
        is Iterable<*> -> {
            append(prefix).append(key)
            if (data.iterator().hasNext()) {
                append(" = [\n")
                data.onEachIndexed { index, item ->
                    appendSavedStateDataString(tabChar, "#$index", item, tab + 1)
                }
                append(prefix).append("]\n")
            } else {
                append(" = []\n")
            }
        }
        else -> {
            append(prefix).append(key).append(" = ").append(data).append("\n")
        }
    }
}

/**
 * Checks if all elements in the iterable satisfy the given predicate with their index.
 *
 * @param predicate A function that takes an index and an element,
 * and returns `true` if the element satisfies the condition, `false` otherwise.
 * @return `true` if all elements satisfy the predicate, `false` if any element does not.
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