package com.composegears.tiamat

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

public actual fun navigationPlatformDefault(isForward: Boolean): ContentTransform =
    if (isForward) ContentTransform(
        targetContentEnter = slideInHorizontally(
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            initialOffsetX = { it / 4 }
        ) + fadeIn(),
        initialContentExit = slideOutHorizontally(
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            targetOffsetX = { -it / 4 }
        ) + fadeOut(),
        sizeTransform = null,
        targetContentZIndex = 1f
    ) else ContentTransform(
        targetContentEnter = slideInHorizontally(
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            initialOffsetX = { -it / 4 }
        ) + fadeIn(),
        initialContentExit = slideOutHorizontally(
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            targetOffsetX = { it / 4 }
        ) + fadeOut(),
        sizeTransform = null,
        targetContentZIndex = -1f
    )