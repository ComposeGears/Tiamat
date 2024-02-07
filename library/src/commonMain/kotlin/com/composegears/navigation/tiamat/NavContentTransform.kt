package com.composegears.navigation.tiamat

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

fun navigationSlideInOut(isForward: Boolean): ContentTransform =
    slideIn(tween()) {
        IntOffset(x = if (isForward) it.width else -it.width, y = 0)
    } togetherWith slideOut(tween()) {
        IntOffset(x = if (isForward) -it.width else it.width, y = 0)
    }

fun navigationFadeInOut(): ContentTransform =
    fadeIn(tween(220, delayMillis = 90)) togetherWith fadeOut(tween(90))

fun navigationSlideInFromBottom() = slideInVertically(tween()) togetherWith fadeOut()

fun navigationSlideOutToBottom() = fadeIn() togetherWith slideOutVertically(tween())