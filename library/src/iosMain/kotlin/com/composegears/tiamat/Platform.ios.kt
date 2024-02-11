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
 * No back button
 */
@Composable
actual fun NavBackHandler(enabled: Boolean, onBackEvent: () -> Unit)  = Unit