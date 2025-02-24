package com.composegears.tiamat

import androidx.compose.animation.ContentTransform

public actual fun navigationPlatformDefault(isForward: Boolean): ContentTransform =
    navigationFadeInOut()