package com.composegears.tiamat.compose

import androidx.compose.runtime.Composable

/**
 * Wrap platform content and provides additional info/providable-s.
 */
@Composable
internal expect fun <Args> NavDestinationScope<Args>.PlatformContentWrapper(
    content: @Composable NavDestinationScope<Args>.() -> Unit,
)

/**
 * 1) We can not call T::class in @Composable functions,
 * 2) Wasm only allows to use `simpleName` for wasm, and other platforms suppose to use`qualifiedName`
 *
 * Workaround is to call it outside of @Composable via regular inline fun.
 */
public expect inline fun <reified T : Any> className(): String