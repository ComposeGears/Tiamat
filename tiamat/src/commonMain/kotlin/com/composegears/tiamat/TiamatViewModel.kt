package com.composegears.tiamat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * ViewModels base class
 */
public abstract class TiamatViewModel {

    private val _viewModelScope = ComposeModelCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    protected val viewModelScope: CoroutineScope = _viewModelScope

    public open fun onClosed(): Unit = Unit

    internal fun close() {
        _viewModelScope.close()
        onClosed()
    }

    internal class ComposeModelCoroutineScope(context: CoroutineContext) : CoroutineScope {
        override val coroutineContext: CoroutineContext = context

        fun close() {
            coroutineContext.cancel()
        }
    }
}

/**
 * Interface for ViewModels that can be saved to a [SavedState]
 */
public interface Saveable {
    public fun saveToSaveState(): SavedState
}