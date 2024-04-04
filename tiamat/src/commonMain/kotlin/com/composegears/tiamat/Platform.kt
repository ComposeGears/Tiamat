package com.composegears.tiamat

import androidx.compose.runtime.Composable

/**
 * @return platform root data storage object
 */
@Composable
internal expect fun rootNavControllersStore(): NavControllersStorage

/**
 * Wrap platform content and provides additional info/providable-s
 */
@Composable
internal expect fun <Args> NavDestinationScope<Args>.PlatformContentWrapper(
    content: @Composable NavDestinationScope<Args>.() -> Unit,
)

/**
 * Platform provided system back handler
 */
@Composable
expect fun NavBackHandler(enabled: Boolean, onBackEvent: () -> Unit)