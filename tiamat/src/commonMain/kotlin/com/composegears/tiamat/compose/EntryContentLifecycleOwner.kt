package com.composegears.tiamat.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.composegears.tiamat.navigation.NavEntry

internal class EntryContentLifecycleOwner(
    private val parentLifecycle: Lifecycle,
    private val entryLifecycle: Lifecycle,
) : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val lifecycleStateObserver = LifecycleEventObserver { _, _ -> updateState() }

    override val lifecycle: Lifecycle = lifecycleRegistry

    init {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        parentLifecycle.addObserver(lifecycleStateObserver)
        entryLifecycle.addObserver(lifecycleStateObserver)
    }

    fun updateState() {
        val parentState = parentLifecycle.currentState
        val entryState = entryLifecycle.currentState
        val targetState = if (parentState.ordinal < entryState.ordinal) parentState else entryState
        if (lifecycle.currentState != targetState) {
            lifecycleRegistry.currentState = targetState
        }
    }

    fun close() {
        parentLifecycle.removeObserver(lifecycleStateObserver)
        entryLifecycle.removeObserver(lifecycleStateObserver)
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
}

@Composable
internal fun rememberEntryContentLifecycleOwner(
    entry: NavEntry<*>
): EntryContentLifecycleOwner {
    val parentLifecycle = LocalLifecycleOwner.current
    return remember(entry) {
        EntryContentLifecycleOwner(parentLifecycle.lifecycle, entry.lifecycle)
    }
}