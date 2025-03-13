package com.composegears.tiamat

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

public actual fun navigationPlatformDefault(isForward: Boolean): ContentTransform =
    if (isForward) ContentTransform(
        targetContentEnter = slideInHorizontally(
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            initialOffsetX = { it }
        ),
        initialContentExit = slideOutHorizontally(
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            targetOffsetX = { -it / 3 }
        ),
        sizeTransform = null,
        targetContentZIndex = 1f
    ) else ContentTransform(
        targetContentEnter = slideInHorizontally(
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            initialOffsetX = { -it / 3 }
        ),
        initialContentExit = slideOutHorizontally(
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            targetOffsetX = { it }
        ),
        sizeTransform = null,
        targetContentZIndex = -1f
    )