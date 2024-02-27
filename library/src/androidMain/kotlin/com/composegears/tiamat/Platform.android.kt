package com.composegears.tiamat

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLifecycleOwner

/**
 * @return platform root data storage object
 */
@Composable
internal actual fun rootDataStore(): DataStorage = rememberRootDataStore()

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

/**
 * Platform provided system back handler
 */
@Composable
actual fun NavBackHandler(enabled: Boolean, onBackEvent: () -> Unit) {
    BackHandler(enabled, onBackEvent)
}