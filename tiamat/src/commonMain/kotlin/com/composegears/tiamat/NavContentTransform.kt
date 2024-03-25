package com.composegears.tiamat

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

fun navigationNone(): ContentTransform = ContentTransform(
    targetContentEnter = EnterTransition.None,
    initialContentExit = ExitTransition.None,
    sizeTransform = null
)

fun navigationSlideInOut(isForward: Boolean) = ContentTransform(
    targetContentEnter = slideIn(tween()) { IntOffset(x = if (isForward) it.width else -it.width, y = 0) },
    initialContentExit = slideOut(tween()) { IntOffset(x = if (isForward) -it.width else it.width, y = 0) },
    sizeTransform = null
)

fun navigationFadeInOut() = ContentTransform(
    targetContentEnter = fadeIn(tween(220, delayMillis = 90)),
    initialContentExit = fadeOut(tween(90)),
    sizeTransform = null
)

fun navigationSlideInFromBottom() = ContentTransform(
    targetContentEnter = slideInVertically(tween()) { it },
    initialContentExit = fadeOut(tween(), targetAlpha = 0.999f),
    sizeTransform = null
)

fun navigationSlideOutToBottom() = ContentTransform(
    targetContentEnter = EnterTransition.None,
    initialContentExit = slideOutVertically(tween()) { it },
    targetContentZIndex = -1f,
    sizeTransform = null
)