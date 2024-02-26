package com.composegears.tiamat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * Internal view model
 *
 * Holds & provide lifecycle
 */
private class LifecycleModel : TiamatViewModel(), LifecycleOwner {
    private val registry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = registry
    private var isClosed = false
    private var isActive = false

    fun onAttach() {
        isActive = true
        //updateState()
    }

    fun onDispose() {
        isActive = false
        //updateState()
    }

    override fun onClosed() {
        super.onClosed()
        isClosed = true
        //updateState()
    }

    private fun onParentStateChanged(state: Lifecycle.State) {
        updateState(state)
    }

    private fun updateState(parentState: Lifecycle.State) {

    }
}

@Composable
fun NavDestinationScope<*>.rememberDestinationLifecycleOwner(): LifecycleOwner {
    val lifecycleModel = rememberViewModel { LifecycleModel() }
    DisposableEffect(lifecycleModel) {
        lifecycleModel.onAttach()
        onDispose {
            lifecycleModel.onDispose()
        }
    }
    return lifecycleModel
}