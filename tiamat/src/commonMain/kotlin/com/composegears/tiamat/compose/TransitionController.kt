package com.composegears.tiamat.compose

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * A controller for managing a transition between two screens.
 *
 * Once created, the controller is in an active state. Calling [update] allows programmatic
 * control over the transition progress. The transition is completed by calling either [finish]
 * or [cancel]. After either method is called, the controller becomes inactive and any further
 * calls to [update], [cancel], or [finish] will throw an error. This is intentional to prevent
 * stale transitions from affecting new navigation operations.
 *
 * @param start The start value of the transition (default: 0f, must be in 0..1)
 * @param end The end value of the transition (default: 1f, must be in 0..1)
 *
 * @throws IllegalArgumentException if start or end values are outside 0..1 range or start > end
 */
public open class TransitionController(
    private val start: Float = 0f,
    private val end: Float = 1f
) {
    private var isInProgress: Boolean = true
    private val _updates = MutableStateFlow<Event>(Event.Update(start))
    internal val updates: StateFlow<Event> = _updates.asStateFlow()

    init {
        require(start <= end) { "`start`($start) value should be less than or equal to `end`($end)" }
        require(start in 0f..1f) { "`start`($start) value should be in range 0..1" }
        require(end in 0f..1f) { "`end`($end) value should be in range 0..1" }
    }

    private fun ensureActive() {
        if (!isInProgress) error("Transition already finished")
    }

    /**
     * Updates the current value of the transition.
     *
     * @param value The new value to set.
     */
    public fun update(value: Float) {
        ensureActive()
        _updates.update { Event.Update(value.coerceIn(start, end)) }
    }

    /**
     * Cancels the transition.
     *
     * @param animationSpec The animation specification for the cancellation.
     */
    public fun cancel(animationSpec: FiniteAnimationSpec<Float> = tween(250)) {
        ensureActive()
        isInProgress = false
        _updates.update { Event.Cancel(start, animationSpec) }
    }

    /**
     * Finishes the transition.
     *
     * @param animationSpec The animation specification for the finish.
     */
    public fun finish(animationSpec: FiniteAnimationSpec<Float> = tween(250)) {
        ensureActive()
        isInProgress = false
        _updates.update { Event.Finish(end, animationSpec) }
    }

    internal sealed class Event(val value: Float) {
        class Update(value: Float) : Event(value)
        class Cancel(value: Float, val animationSpec: FiniteAnimationSpec<Float>) : Event(value)
        class Finish(value: Float, val animationSpec: FiniteAnimationSpec<Float>) : Event(value)
    }
}