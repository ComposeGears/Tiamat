package com.composegears.tiamat

import android.util.Log
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
        Log.i("AAA", "onAttach")
        isActive = true
        updateState()
    }

    fun onDispose() {
        Log.i("AAA", "onDispose")
        isActive = false
        parentState = null
        updateState()
    }

    override fun onClosed() {
        Log.i("AAA", "onClosed")
        super.onClosed()
        isClosed = true
        updateState()
    }

    // observe parent lifecycle changes
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.i("AAA", "onStateChanged = $event | ${event.targetState}")
        parentState = event.targetState
        updateState()
    }

    private fun updateState() {
        if (parentState != null) {
            registry.currentState = parentState!!
            // todo handle base on isActive
        } else {
            if (isClosed) registry.currentState = Lifecycle.State.DESTROYED
            else registry.currentState = Lifecycle.State.INITIALIZED
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