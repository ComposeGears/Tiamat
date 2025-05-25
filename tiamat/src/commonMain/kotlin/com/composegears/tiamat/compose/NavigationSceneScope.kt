package com.composegears.tiamat.compose

import androidx.compose.runtime.Composable
import com.composegears.tiamat.navigation.NavEntry

/**
 * Scope for the `scene` content within a [NavigationScene].
 *
 * This scope provides the [EntryContent] composable function, which is used to render
 * the content of a specific [NavEntry]. This allows for flexible layout arrangements
 * where different navigation entries can be placed explicitly by the user within the `scene`.
 *
 * Example usage:
 * ```
 * NavigationScene(navController, destinations) { // this: NavigationSceneScope
 *     val currentEntry by navController.currentNavEntryAsState()
 *     AnimatedContent(
 *         targetState = currentEntry,
 *         contentKey = { it?.contentKey() },
 *         transitionSpec = { navigationFadeInOut() }
 *     ) {
 *         EntryContent(it)
 *     }
 * }
 * ```
 */
public class NavigationSceneScope internal constructor(
    private val entryContent: @Composable (NavEntry<*>) -> Unit,
) {
    /**
     * Renders the content of the provided [NavEntry].
     *
     * If the `entry` is null, this function does nothing.
     *
     * @param entry The [NavEntry] whose content should be rendered.
     */
    @Composable
    public fun EntryContent(entry: NavEntry<*>?) {
        if (entry != null) entryContent(entry)
    }
}