package com.composegears.navigation.tiamat

import androidx.compose.runtime.Composable

/**
 * Global in-memory data storage
 */
private val globalDataStorage = DataStorage()

// TODO check with iOs ppl
@Composable
internal actual fun rootDataStore(): DataStorage = globalDataStorage

/**
 * No back button
 */
@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBackEvent: () -> Unit)  = Unit