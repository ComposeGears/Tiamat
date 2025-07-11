package com.composegears.tiamat.compose

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Internal view model
 *
 * Holds & provide lifecycle
 */
private class LifecycleModel : ViewModel(), LifecycleOwner, LifecycleEventObserver {
    @SuppressLint("StaticFieldLeak")
    private val registry = LifecycleRegistry(this)
    private var parentState: Lifecycle.State? = null
    private var isClosed = false
    private var isActive = false

    override val lifecycle: Lifecycle get() = registry

    init {
        registry.currentState = Lifecycle.State.CREATED
    }

    fun onAttach() {
        isActive = true
        updateState()
    }

    fun onDispose() {
        isActive = false
        updateState()
        parentState = null
    }

    override fun onCleared() {
        super.onCleared()
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
@Suppress("ViewModelInjection")
internal fun rememberDestinationLifecycleOwner(): LifecycleOwner {
    val lifecycleModel = viewModel { LifecycleModel() }
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