package com.composegears.tiamat.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * Base class for view models.
 */
public abstract class TiamatViewModel {

    private val _viewModelScope = ModelCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * A coroutine scope tied to the lifecycle of this view model.
     * Automatically cancelled when the view model is closed.
     */
    protected val viewModelScope: CoroutineScope = _viewModelScope

    /**
     * Called when this view model is being closed.
     * Override this method to clean up resources or perform final operations.
     */
    public open fun onClosed(): Unit = Unit

    internal fun close() {
        _viewModelScope.close()
        onClosed()
    }

    internal class ModelCoroutineScope(context: CoroutineContext) : CoroutineScope {
        override val coroutineContext: CoroutineContext = context

        fun close() {
            coroutineContext.cancel()
        }
    }
}

/**
 * Interface for view models that can save their state.
 */
public interface Saveable {

    /**
     * Saves the current state of the view model.
     *
     * @return A SavedState containing the view model's state
     */
    public fun saveToSaveState(): SavedState
}