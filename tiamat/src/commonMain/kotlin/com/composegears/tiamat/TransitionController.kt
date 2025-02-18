package com.composegears.tiamat

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * A controller for managing a transition between two screens.
 *
 * @param start The start value of the transition.
 * @param end The end value of the transition.
 */
public class TransitionController(
    private val start: Float = 0f,
    private val end: Float = 1f
) {
    private var isInProgress: Boolean = true
    private val _updates = MutableStateFlow<Event>(Event.Update(start))
    internal val updates: StateFlow<Event> = _updates.asStateFlow()

    init {
        if (start >= end) error("`start`($start) value should be smaller then `end`($end)")
        if (start !in 0f..1f) error("`start`($start) value should be in range 0..1")
        if (end !in 0f..1f) error("`end`($end) value should be in range 0..1")
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