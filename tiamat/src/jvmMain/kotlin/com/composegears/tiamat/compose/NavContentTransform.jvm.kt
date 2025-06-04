package com.composegears.tiamat.compose

import androidx.compose.animation.ContentTransform

public actual fun navigationPlatformDefault(isForward: Boolean): ContentTransform =
    navigationFadeInOut()