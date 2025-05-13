package com.composegears.tiamat.compose

import androidx.compose.animation.ContentTransform

public data class TransitionData(
    val contentTransform: ContentTransform?,
    val transitionController: TransitionController?,
)