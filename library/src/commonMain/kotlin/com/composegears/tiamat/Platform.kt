package com.composegears.tiamat

import androidx.compose.runtime.Composable

/**
 * @return platform root data storage object
 */
@Composable
internal expect fun rootDataStore(): DataStorage

/**
 * Platform provided system back handler
 */
@Composable
internal expect fun PlatformBackHandler(enabled: Boolean, onBackEvent: () -> Unit)