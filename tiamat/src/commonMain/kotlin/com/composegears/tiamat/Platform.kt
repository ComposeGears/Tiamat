package com.composegears.tiamat

import androidx.compose.runtime.Composable

/**
 * @return platform root NavControllers storage object.
 */
@Composable
internal expect fun rootNavControllersStore(): NavControllersStorage

/**
 * Wrap platform content and provides additional info/providable-s.
 */
@Composable
internal expect fun <Args> NavDestinationScope<Args>.PlatformContentWrapper(
    content: @Composable NavDestinationScope<Args>.() -> Unit,
)

/**
 * We can not call T::class in @Composable functions,
 *
 * workaround is to call it outside of @Composable via regular inline fun.
 */
public expect inline fun <reified T : Any> className(): String