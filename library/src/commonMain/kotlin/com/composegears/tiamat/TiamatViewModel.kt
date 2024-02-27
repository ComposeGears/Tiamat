package com.composegears.tiamat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * ViewModels base class
 */
abstract class TiamatViewModel {

    protected val viewModelScope = ComposeModelCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    open fun onClosed() = Unit

    internal fun close() {
        viewModelScope.close()
        onClosed()
    }

    protected class ComposeModelCoroutineScope(context: CoroutineContext) : CoroutineScope {
        override val coroutineContext: CoroutineContext = context

        fun close() {
            coroutineContext.cancel()
        }
    }
}