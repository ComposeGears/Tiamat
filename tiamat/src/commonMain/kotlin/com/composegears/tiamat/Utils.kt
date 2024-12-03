package com.composegears.tiamat

/**
 * SaveState declaration alias
 */
public typealias SavedState = Map<String, Any?>

// TODO add doc
public fun SavedState.toHumanReadableString(
    tabChar: String = "    ",
    initialTabSize: Int = 0,
): String = StringBuilder()
    .apply { appendBeautifulString(tabChar, "Data", this@toHumanReadableString, initialTabSize) }
    .toString()

private fun StringBuilder.appendBeautifulString(tabChar: String, key: String, data: Any?, tab: Int) {
    val prefix = tabChar.repeat(tab)
    when (data) {
        is Map<*, *> -> {
            append(prefix).append(key)
            if (data.isNotEmpty()) {
                append(" = {\n")
                data.onEach {
                    appendBeautifulString(tabChar, it.key.toString(), it.value, tab + 1)
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
                    appendBeautifulString(tabChar, "#$index", item, tab + 1)
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