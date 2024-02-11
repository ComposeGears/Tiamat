package com.composegears.tiamat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf

val LocalNavBackHandler = staticCompositionLocalOf { NavBackHandler() }

/**
 * Global in-memory data storage
 */
private val globalDataStorage = DataStorage()

@Composable
internal actual fun rootDataStore(): DataStorage = globalDataStorage

@Composable
actual fun NavBackHandler(enabled: Boolean, onBackEvent: () -> Unit) {
    if (enabled) {
        val backHandler = LocalNavBackHandler.current
        DisposableEffect(Unit) {
            val callback = { onBackEvent() }
            backHandler.add(callback)
            onDispose {
                backHandler.remove(callback)
            }
        }
    }
}