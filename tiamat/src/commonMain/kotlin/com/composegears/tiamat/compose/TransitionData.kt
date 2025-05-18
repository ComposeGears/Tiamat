package com.composegears.tiamat.compose

import androidx.compose.animation.ContentTransform

/**
 * Holds data for customizing navigation transitions.
 *
 * @property contentTransform Optional animation specification for the transition
 * @property transitionController Optional controller for managing the transition progress
 */
public data class TransitionData(
    val contentTransform: ContentTransform?,
    val transitionController: TransitionController?,
)