package com.composegears.tiamat.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.composegears.tiamat.rememberDestinationLifecycleOwner

/**
 * Wrap platform content and provides additional info/providable-s
 */
@Composable
internal actual fun <Args> NavDestinationScope<Args>.PlatformContentWrapper(
    content: @Composable NavDestinationScope<Args>.() -> Unit
) {
    val lifecycleOwner = rememberDestinationLifecycleOwner()
    CompositionLocalProvider(
        LocalLifecycleOwner provides lifecycleOwner
    ) {
        content()
    }
}

public actual inline fun <reified T : Any> className(): String = T::class.qualifiedName!!
