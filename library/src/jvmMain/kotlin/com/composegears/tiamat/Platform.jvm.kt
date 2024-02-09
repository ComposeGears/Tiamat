package com.composegears.tiamat

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent

/**
 * Global in-memory data storage
 */
private val globalDataStorage = DataStorage()

@Composable
internal actual fun rootDataStore(): DataStorage = globalDataStorage

@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBackEvent: () -> Unit) {
    Box(Modifier.onKeyEvent {
        if (it.key == Key.Escape && enabled) {
            onBackEvent()
            true
        } else false
    })
}