package com.composegears.tiamat

import androidx.compose.runtime.Composable

/**
 * Global in-memory data storage
 */
private val globalDataStorage = DataStorage()

// TODO check with iOs ppl
@Composable
internal actual fun rootDataStore(): DataStorage = globalDataStorage

/**
 * Wrap platform content and provides additional info/providable-s
 */
@Composable
internal actual fun <Args> NavDestinationScope<Args>.PlatformContentWrapper(
    content: @Composable NavDestinationScope<Args>.() -> Unit
) {
    content()
}

/**
 * No back button
 */
@Composable
actual fun NavBackHandler(enabled: Boolean, onBackEvent: () -> Unit) = Unit