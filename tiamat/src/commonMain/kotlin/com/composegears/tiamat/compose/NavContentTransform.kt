package com.composegears.tiamat.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

/**
 * Creates a ContentTransform with no animation.
 *
 * Use this when you want an immediate transition with no animation effects.
 *
 * @return A ContentTransform with no enter or exit transitions
 */
public fun navigationNone(): ContentTransform =
    ContentTransform(
        targetContentEnter = EnterTransition.None,
        initialContentExit = ExitTransition.None,
        sizeTransform = null
    )

/**
 * Creates a horizontal slide animation for navigation transitions.
 *
 * New content slides in from the right (when going forward) or from the left (when going backward).
 * Current content slides out to the left (when going forward) or to the right (when going backward).
 *
 * @param isForward Whether the navigation is moving forward (true) or backward (false)
 * @return A ContentTransform with horizontal slide animations
 */
public fun navigationSlideInOut(isForward: Boolean): ContentTransform =
    ContentTransform(
        targetContentEnter = slideIn(tween()) { IntOffset(x = if (isForward) it.width else -it.width, y = 0) },
        initialContentExit = slideOut(tween()) { IntOffset(x = if (isForward) -it.width else it.width, y = 0) },
        sizeTransform = null
    )

/**
 * Creates a fade animation for navigation transitions.
 *
 * New content fades in while current content fades out.
 *
 * @return A ContentTransform with fade in/out animations
 */
public fun navigationFadeInOut(): ContentTransform =
    ContentTransform(
        targetContentEnter = fadeIn(tween(220, delayMillis = 90)),
        initialContentExit = fadeOut(tween(90)),
        sizeTransform = null
    )

/**
 * Creates a transition where new content slides in from the bottom.
 *
 * New content slides up from the bottom of the screen.
 * Current content slightly fades out.
 *
 * @return A ContentTransform with slide-in-from-bottom animation
 */
public fun navigationSlideInFromBottom(): ContentTransform =
    ContentTransform(
        targetContentEnter = slideInVertically(tween()) { it },
        initialContentExit = fadeOut(tween(), targetAlpha = 0.999f),
        sizeTransform = null
    )

/**
 * Creates a transition where current content slides out to the bottom.
 *
 * Current content slides down to the bottom of the screen.
 * New content appears without animation behind the exiting content.
 *
 * @return A ContentTransform with slide-out-to-bottom animation
 */
public fun navigationSlideOutToBottom(): ContentTransform =
    ContentTransform(
        targetContentEnter = EnterTransition.None,
        initialContentExit = slideOutVertically(tween()) { it },
        targetContentZIndex = -1f,
        sizeTransform = null
    )

/**
 * Creates a platform-specific default navigation transition.
 *
 * @param isForward Whether the navigation is moving forward (true) or backward (false)
 * @return A platform-specific ContentTransform
 */
public expect fun navigationPlatformDefault(isForward: Boolean): ContentTransform