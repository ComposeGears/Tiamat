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
 * The producer coroutine is launched when the composable enters the composition
 * and canceled when entry is removed from navigation stack.
 *
 * @param initialValue The initial value of the state.
 * @param producer A suspend function that can update the state value.
 * It has access to [ProduceStateScope] for updating the state and awaiting disposal.
 *
 * @return A [State] that holds the current value produced by the coroutine.
 */
@Composable
@TiamatExperimentalApi
@Suppress("RememberMissing")
public fun <T> produceRetainedState(
    initialValue: T,
    producer: suspend ProduceStateScope<T>.() -> Unit
): State<T> {
    val state = retain { mutableStateOf(initialValue) }
    RetainedEffect(Unit) {
        val context = Job() + EmptyCoroutineContext
        val scope = CoroutineScope(context)
        scope.launch {
            ProduceRetainedStateScopeImpl(state, context).producer()
        }
        onRetire {
            context.cancel()
        }
    }
    return state
}

/**
 * Creates a [State] that is retained across recompositions and
 * runs a producer coroutine to update its value.
 *
 * The producer coroutine is launched when the composable enters the composition
 * and is cancelled when entry is removed from navigation stack.
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
@Suppress("RememberMissing")
public fun <T> produceRetainedState(
    initialValue: T,
    vararg keys: Any?,
    producer: suspend ProduceStateScope<T>.() -> Unit
): State<T> {
    val state = retain(*keys) { mutableStateOf(initialValue) }
    RetainedEffect(*keys) {
        val context = SupervisorJob() + EmptyCoroutineContext
        val scope = CoroutineScope(context)
        scope.launch {
            ProduceRetainedStateScopeImpl(state, context).producer()
        }
        onRetire {
            context.cancel()
        }
    }
    return state
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