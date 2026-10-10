package com.composegears.tiamat.compose

import androidx.compose.runtime.*
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import com.composegears.tiamat.TiamatExperimentalApi
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Creates a [State] that is retained across recompositions and
 * runs a producer coroutine to update its value.
 *
 * Works the same way as [produceState], but the producer coroutine keeps running while the entry
 * is kept in the navigation stack (e.g. when navigating forward and returning back) and is canceled
 * when entry is removed from navigation stack.
 *
 * The producer coroutine runs in the same context as [retainCoroutineScope].
 *
 * @param initialValue The initial value of the state.
 * @param producer A suspend function that can update the state value.
 * It has access to [ProduceStateScope] for updating the state and awaiting disposal.
 *
 * @return A [State] that holds the current value produced by the coroutine.
 */
@Composable
@TiamatExperimentalApi
public fun <T> produceRetainedState(
    initialValue: T,
    producer: suspend ProduceStateScope<T>.() -> Unit
): State<T> = produceRetainedStateImpl(
    initialValue = initialValue,
    producer = producer,
)

/**
 * Creates a [State] that is retained across recompositions and
 * runs a producer coroutine to update its value.
 *
 * Works the same way as [produceState], but the producer coroutine keeps running while the entry
 * is kept in the navigation stack (e.g. when navigating forward and returning back) and is canceled
 * when entry is removed from navigation stack.
 *
 * When [keys] change, the running producer is canceled and a new one is launched.
 * The state is not reset to [initialValue], it keeps the last value until the new producer updates it.
 *
 * The producer coroutine runs in the same context as [retainCoroutineScope].
 *
 * @param initialValue The initial value of the state.
 * @param keys Keys that control when the producer coroutine is restarted.
 * @param producer A suspend function that can update the state value.
 * It has access to [ProduceStateScope] for updating the state and awaiting disposal.
 *
 * @return A [State] that holds the current value produced by the coroutine.
 */
@Composable
@TiamatExperimentalApi
public fun <T> produceRetainedState(
    initialValue: T,
    vararg keys: Any?,
    producer: suspend ProduceStateScope<T>.() -> Unit
): State<T> = produceRetainedStateImpl(
    initialValue = initialValue,
    keys = keys,
    producer = producer
)

/**
 * Creates a [CoroutineScope] that is retained across recompositions and configuration changes.
 *
 * Unlike [rememberCoroutineScope], the scope survives while the entry is kept in the navigation
 * stack (e.g. when navigating forward and returning back) and is canceled
 * when entry is removed from navigation stack.
 *
 * The scope uses a [SupervisorJob], so a failure of one child coroutine does not cancel the others.
 * Coroutines are dispatched via `Dispatchers.Main.immediate`, or run in the caller's context
 * ([EmptyCoroutineContext]) on platforms where `Dispatchers.Main` is not available.
 *
 * @return A [CoroutineScope] bound to the retain lifecycle of the current entry.
 */
@Composable
@TiamatExperimentalApi
public fun retainCoroutineScope(): CoroutineScope {
    val scope = retain { CoroutineScope(retainedCoroutineContext()) }
    RetainedEffect(scope) {
        onRetire {
            scope.cancel()
        }
    }
    return scope
}

// --- Implementations -------------------------------------------------------------------------------------------------

@Composable
private fun <T> produceRetainedStateImpl(
    initialValue: T,
    vararg keys: Any?,
    producer: suspend ProduceStateScope<T>.() -> Unit
): State<T> {
    val state = retain { mutableStateOf(initialValue) }
    RetainedEffect(*keys) {
        val scope = CoroutineScope(retainedCoroutineContext())
        scope.launch {
            ProduceRetainedStateScopeImpl(state, coroutineContext).producer()
        }
        onRetire {
            scope.cancel()
        }
    }
    return state
}

private fun retainedCoroutineContext(): CoroutineContext {
    val dispatcher = try {
        Dispatchers.Main.immediate
    } catch (_: NotImplementedError) {
        EmptyCoroutineContext
    } catch (_: IllegalStateException) {
        EmptyCoroutineContext
    }
    return dispatcher + SupervisorJob()
}

private class ProduceRetainedStateScopeImpl<T>(
    state: MutableState<T>,
    override val coroutineContext: CoroutineContext,
) : ProduceStateScope<T>, MutableState<T> by state {

    override suspend fun awaitDispose(onDispose: () -> Unit): Nothing {
        try {
            suspendCancellableCoroutine<Nothing> {}
        } finally {
            onDispose()
        }
    }
}