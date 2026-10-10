package com.composegears.tiamat.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.retain.LocalRetainedValuesStoreProvider
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.SavedState
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import androidx.savedstate.read
import androidx.savedstate.savedState
import com.composegears.tiamat.navigation.NavEntry

/**
 * CompositionLocal that provides access to the current NavEntry.
 */
internal val LocalNavEntry = staticCompositionLocalOf<NavEntry<*>?> { null }

@Composable
@Suppress("UNCHECKED_CAST")
internal fun <Args : Any> NavEntryContent(
    entry: NavEntry<Args>
) {
    val destination = entry.destination
    if (destination is ComposeNavDestination<Args>) Box {
        val host = rememberNavEntryHost(entry)
        // `LocalRetainedValuesStoreProvider` notifies the store about content enter/exit composition,
        // so values exited while content is displayed (e.g. retain key change) are retired immediately
        // and values exited along with the content (e.g. entry goes to backstack) are retained
        LocalRetainedValuesStoreProvider(entry.retainedValuesStore) {
            CompositionLocalProvider(
                LocalNavEntry provides entry,
                LocalLifecycleOwner provides host,
                LocalSaveableStateRegistry provides host,
                LocalSavedStateRegistryOwner provides host,
                LocalViewModelStoreOwner provides host,
            ) {
                EntryDestinationContent(entry, destination)
            }
            // save state when `this entry`/`parent entry` stops being displayed
            // note: must be declared after the content, so it is disposed before the content
            DisposableEffect(entry) {
                entry.attachToUI()
                entry.setSavedStateSaver(host::performSave)
                onDispose {
                    entry.setSavedStateSaver(null)
                    host.close()
                    if (entry.isAttachedToNavController) entry.savedState = host.performSave()
                    entry.detachFromUI()
                }
            }
        }
    }
}

@Composable
private fun <Args : Any> EntryDestinationContent(
    entry: NavEntry<Args>,
    destination: ComposeNavDestination<Args>,
) {
    val scope = remember(entry) { NavDestinationScope(entry) }
    val contentWrapper = remember(destination.extensions) {
        destination.extensions.filterIsInstance<ContentExtension<Args>>().combine()
    }
    with(scope) {
        contentWrapper {
            with(destination) {
                Content()
            }
        }
    }
}

@Composable
private fun rememberNavEntryHost(entry: NavEntry<*>): NavEntryHost {
    val parentLifecycle = LocalLifecycleOwner.current.lifecycle
    val parentSaveableStateRegistry = LocalSaveableStateRegistry.current
    return remember(entry) {
        NavEntryHost(
            entry = entry,
            parentLifecycle = parentLifecycle,
            parentSaveableStateRegistry = parentSaveableStateRegistry,
        )
    }
}

/**
 * Hosts the entry content: provides entry bound lifecycle, saveable state, saved state registry and view models.
 *
 * - lifecycle - the min state of parent and entry lifecycles
 * - saveable state - restored from & saved to [NavEntry.savedState], also holds [savedStateRegistry] state
 * - view models - stored in [NavEntry.viewModelStore], `SavedStateHandle` is supported
 */
@Suppress("UNCHECKED_CAST")
private class NavEntryHost(
    private val entry: NavEntry<*>,
    private val parentLifecycle: Lifecycle,
    parentSaveableStateRegistry: SaveableStateRegistry?,
) : LifecycleOwner,
    SavedStateRegistryOwner,
    ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory,
    SaveableStateRegistry by SaveableStateRegistry(
        restoredValues = entry.savedState as? Map<String, List<Any?>>?,
        canBeSaved = { parentSaveableStateRegistry?.canBeSaved(it) ?: true }
    ) {

    private companion object {
        const val SAVED_STATE_REGISTRY_KEY = "tiamat:savedStateRegistry"
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val lifecycleStateObserver = LifecycleEventObserver { _, _ -> updateLifecycleState() }
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

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
        // saved state registry and saved state handles must be set up while lifecycle is INITIALIZED
        savedStateRegistryController.performRestore(consumeRestored(SAVED_STATE_REGISTRY_KEY) as? SavedState)
        registerProvider(SAVED_STATE_REGISTRY_KEY) {
            val state = savedState()
            savedStateRegistryController.performSave(state)
            if (state.read { isEmpty() }) null else state
        }
        enableSavedStateHandles()
        // start following parent & entry lifecycles
        parentLifecycle.addObserver(lifecycleStateObserver)
        entry.lifecycle.addObserver(lifecycleStateObserver)
    }

    private fun updateLifecycleState() {
        val parentState = parentLifecycle.currentState
        val entryState = entry.lifecycle.currentState
        val targetState = if (parentState.ordinal < entryState.ordinal) parentState else entryState
        if (lifecycleRegistry.currentState != targetState) {
            lifecycleRegistry.currentState = targetState
        }
    }

    fun close() {
        parentLifecycle.removeObserver(lifecycleStateObserver)
        entry.lifecycle.removeObserver(lifecycleStateObserver)
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
}
