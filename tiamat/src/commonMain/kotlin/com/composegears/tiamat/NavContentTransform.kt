package com.composegears.tiamat

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

public fun navigationNone(): ContentTransform =
    ContentTransform(
        targetContentEnter = EnterTransition.None,
        initialContentExit = ExitTransition.None,
        sizeTransform = null
    )

public fun navigationSlideInOut(isForward: Boolean): ContentTransform =
    ContentTransform(
        targetContentEnter = slideIn(tween()) { IntOffset(x = if (isForward) it.width else -it.width, y = 0) },
        initialContentExit = slideOut(tween()) { IntOffset(x = if (isForward) -it.width else it.width, y = 0) },
        sizeTransform = null
    )

public fun navigationFadeInOut(): ContentTransform =
    ContentTransform(
        targetContentEnter = fadeIn(tween(220, delayMillis = 90)),
        initialContentExit = fadeOut(tween(90)),
        sizeTransform = null
    )

public fun navigationSlideInFromBottom(): ContentTransform =
    ContentTransform(
        targetContentEnter = slideInVertically(tween()) { it },
        initialContentExit = fadeOut(tween(), targetAlpha = 0.999f),
        sizeTransform = null
    )

public fun navigationSlideOutToBottom(): ContentTransform =
    ContentTransform(
        targetContentEnter = EnterTransition.None,
        initialContentExit = slideOutVertically(tween()) { it },
        targetContentZIndex = -1f,
        sizeTransform = null
    )

public expect fun navigationPlatformDefault(isForward: Boolean): ContentTransform