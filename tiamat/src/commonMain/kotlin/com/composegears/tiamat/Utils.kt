package com.composegears.tiamat

import androidx.savedstate.read
import com.composegears.tiamat.navigation.SavedState

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
            append(prefix).append(key).append(" = ").also {
                when (data) {
                    null -> append("null")
                    is androidx.savedstate.SavedState -> {
                        append("Serialized:")
                        data.read { append(contentDeepToString()) }
                    }
                    else -> append(data.toString())
                }
            }.append("\n")
        }
    }
}

@Retention(AnnotationRetention.BINARY)
internal annotation class ExcludeFromTests