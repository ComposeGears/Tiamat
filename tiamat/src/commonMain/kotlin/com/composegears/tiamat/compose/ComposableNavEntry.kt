package com.composegears.tiamat.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.retain.LocalRetainedValuesStore
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
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
        val entryLifecycleOwner = rememberEntryContentLifecycleOwner(entry)
        val entrySaveableStateRegistry = rememberEntrySaveableStateRegistry(entry)
        // Lifecycle, RetainedStore and SaveableStateRegistry had no dependencies
        CompositionLocalProvider(
            LocalNavEntry provides entry,
            LocalLifecycleOwner provides entryLifecycleOwner,
            LocalRetainedValuesStore provides entry.retainedValuesStore,
            LocalSaveableStateRegistry provides entrySaveableStateRegistry,
        ) {
            // depends on LocalLifecycleOwner
            // provides LocalSaveableStateRegistry && LocalSavedStateRegistryOwner bound to entry
            rememberSaveableStateHolder().SaveableStateProvider(entry.uuid) {
                // depends on LocalSavedStateRegistryOwner && Lifecycle
                val entryViewModelStoreOwner = rememberEntryContentViewModelStoreOwner(entry)
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides entryViewModelStoreOwner
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
                            entryLifecycleOwner.close()
                            if (entry.isAttachedToNavController) entry.savedState =
                                entrySaveableStateRegistry.performSave()
                            entry.detachFromUI()
                        }
                    }
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
    val parentLifecycle = LocalLifecycleOwner.current
    return remember(entry) {
        EntryContentLifecycleOwner(parentLifecycle.lifecycle, entry.lifecycle)
    }
}

@Composable
private fun rememberEntryContentViewModelStoreOwner(
    entry: NavEntry<*>,
): ViewModelStoreOwner {
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
    return remember(entry) {
        object :
            ViewModelStoreOwner,
            SavedStateRegistryOwner by savedStateRegistryOwner,
            HasDefaultViewModelProviderFactory {
            override val viewModelStore: ViewModelStore
                get() = entry.viewModelStore

            override val defaultViewModelProviderFactory: ViewModelProvider.Factory
                get() = SavedStateViewModelFactory()

            override val defaultViewModelCreationExtras: CreationExtras
                get() = MutableCreationExtras().also {
                    it[SAVED_STATE_REGISTRY_OWNER_KEY] = this
                    it[VIEW_MODEL_STORE_OWNER_KEY] = this
                }

            init {
                require(this.lifecycle.currentState == Lifecycle.State.INITIALIZED) {
                    "The Lifecycle state is already beyond INITIALIZED. The " +
                        "rememberEntryContentViewModelStoreOwner requires adding the " +
                        "SavedStateNavEntryDecorator to ensure support for " +
                        "SavedStateHandles."
                }
                enableSavedStateHandles()
            }
        }
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