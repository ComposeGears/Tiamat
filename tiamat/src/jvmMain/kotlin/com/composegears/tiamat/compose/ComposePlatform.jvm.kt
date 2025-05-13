package com.composegears.tiamat.compose

import androidx.compose.runtime.Composable

/**
 * Wrap platform content and provides additional info/providable-s
 */
@Composable
internal actual fun <Args> NavDestinationScope<Args>.PlatformContentWrapper(
    content: @Composable NavDestinationScope<Args>.() -> Unit
) {
    content()
}

public actual inline fun <reified T : Any> className(): String = T::class.qualifiedName!!