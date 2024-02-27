package com.composegears.tiamat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * Internal view model
 *
 * Holds & provide lifecycle
 */
private class LifecycleModel : TiamatViewModel(), LifecycleOwner, LifecycleEventObserver {
    private val registry = LifecycleRegistry(this)
    private var parentState: Lifecycle.State? = null
    private var isClosed = false
    private var isActive = false

    override val lifecycle: Lifecycle get() = registry

    fun onAttach() {
        isActive = true
        updateState()
    }

    fun onDispose() {
        isActive = false
        updateState()
        parentState = null
    }

    override fun onClosed() {
        super.onClosed()
        isClosed = true
        updateState()
    }

    // observe parent lifecycle changes
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        parentState = event.targetState
        updateState()
    }

    private fun updateState() {
        val current = registry.currentState
        val isCreated = parentState?.isAtLeast(Lifecycle.State.CREATED) ?: false
        val newState = when {
            isClosed -> Lifecycle.State.DESTROYED
            isCreated && isActive -> parentState!!
            isCreated && !isActive -> Lifecycle.State.CREATED
            else -> current
        }
        if (newState != current) {
            registry.currentState = newState
        }
    }
}

@Composable
fun NavDestinationScope<*>.rememberDestinationLifecycleOwner(): LifecycleOwner {
    val lifecycleModel = rememberViewModel { LifecycleModel() }
    val parentLifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleModel) {
        lifecycleModel.onAttach()
        parentLifecycleOwner.lifecycle.addObserver(lifecycleModel)
        onDispose {
            parentLifecycleOwner.lifecycle.removeObserver(lifecycleModel)
            lifecycleModel.onDispose()
        }
    }
    return lifecycleModel
}