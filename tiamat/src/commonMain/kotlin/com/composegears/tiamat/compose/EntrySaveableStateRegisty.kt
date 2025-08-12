package com.composegears.tiamat.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.composegears.tiamat.navigation.NavEntry

@Composable
@Suppress("UNCHECKED_CAST")
internal fun rememberEntrySaveableStateRegistry(
    entry: NavEntry<*>
): SaveableStateRegistry {
    val parentRegistry = LocalSaveableStateRegistry.current
    return remember(entry) {
        SaveableStateRegistry(
            restoredValues = entry.savedState as? Map<String, List<Any?>>?,
            canBeSaved = { parentRegistry?.canBeSaved(it) ?: true }
        )
    }
}