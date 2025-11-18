package com.composegears.tiamat.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.retain.LocalRetainedValuesStore
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.composegears.tiamat.navigation.NavEntry

/**
 * CompositionLocal that provides access to the current NavEntry.
 */
internal val LocalNavEntry = staticCompositionLocalOf<NavEntry<*>?> { null }

@Composable
@Suppress("CognitiveComplexMethod", "UNCHECKED_CAST")
internal fun <Args : Any> NavEntryContent(
    entry: NavEntry<Args>
) {
    val destination = entry.destination
    if (destination is ComposeNavDestination<Args>) Box {
        val entrySaveableStateRegistry = rememberEntrySaveableStateRegistry(entry)
        val entryContentLifecycleOwner = rememberEntryContentLifecycleOwner(entry)
        // display content
        CompositionLocalProvider(
            LocalSaveableStateRegistry provides entrySaveableStateRegistry,
            LocalLifecycleOwner provides entryContentLifecycleOwner,
            LocalRetainedValuesStore provides entry.retainedValuesStore,
            LocalViewModelStoreOwner provides entry,
            LocalNavEntry provides entry,
        ) {
            val scope = remember(entry) { NavDestinationScope(entry) }
            // entry content
            with(scope) {
                // extensions before-content
                destination.extensions.onEach {
                    if (it is ContentExtension && it.getType() == ContentExtension.Type.Underlay) with(it) {
                        Content()
                    }
                }
                // destination content
                with(destination) {
                    Content()
                }
                // extensions after-content
                destination.extensions.onEach {
                    if (it is ContentExtension && it.getType() == ContentExtension.Type.Overlay) with(it) {
                        Content()
                    }
                }
            }
            // save state when `this entry`/`parent entry` stops being displayed
            DisposableEffect(entry) {
                entry.attachToUI()
                entry.setSavedStateSaver(entrySaveableStateRegistry::performSave)
                // save state handle
                onDispose {
                    entry.setSavedStateSaver(null)
                    entryContentLifecycleOwner.close()
                    if (entry.isAttachedToNavController) entry.savedState = entrySaveableStateRegistry.performSave()
                    entry.detachFromUI()
                }
            }
        }
    }
}

@Composable
@Suppress("UNCHECKED_CAST")
private fun rememberEntrySaveableStateRegistry(
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

@Composable
private fun rememberEntryContentLifecycleOwner(
    entry: NavEntry<*>
): EntryContentLifecycleOwner {
    val parentLifecycle = runCatching { LocalLifecycleOwner.current }.getOrNull()
    return remember(entry) {
        EntryContentLifecycleOwner(parentLifecycle?.lifecycle, entry.lifecycle)
    }
}

private class EntryContentLifecycleOwner(
    private val parentLifecycle: Lifecycle?,
    private val entryLifecycle: Lifecycle,
) : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val lifecycleStateObserver = LifecycleEventObserver { _, _ -> updateState() }

    override val lifecycle: Lifecycle = lifecycleRegistry

    init {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        parentLifecycle?.addObserver(lifecycleStateObserver)
        entryLifecycle.addObserver(lifecycleStateObserver)
    }

    fun updateState() {
        val parentState = parentLifecycle?.currentState ?: Lifecycle.State.RESUMED
        val entryState = entryLifecycle.currentState
        val targetState = if (parentState.ordinal < entryState.ordinal) parentState else entryState
        if (lifecycle.currentState != targetState) {
            lifecycleRegistry.currentState = targetState
        }
    }

    fun close() {
        parentLifecycle?.removeObserver(lifecycleStateObserver)
        entryLifecycle.removeObserver(lifecycleStateObserver)
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
}