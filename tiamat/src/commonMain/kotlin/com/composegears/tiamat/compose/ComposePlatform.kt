package com.composegears.tiamat.compose

import androidx.compose.runtime.Composable

/**
 * Wraps destination content with platform-specific functionality.
 *
 * This function is used internally to provide platform-specific behavior and
 * Composition locals when rendering destination content.
 *
 * @param Args The type of arguments the destination accepts
 * @param content The destination content to wrap
 */
@Composable
internal expect fun <Args> NavDestinationScope<Args>.PlatformContentWrapper(
    content: @Composable NavDestinationScope<Args>.() -> Unit,
)

/**
 * Gets a string representation of a class name.
 *
 * This function works around limitations in Compose and cross-platform differences:
 * 1. We cannot call T::class in @Composable functions
 * 2. Wasm only allows using `simpleName` while other platforms use `qualifiedName`
 *
 * @param T The type to get the class name for
 * @return A string representing the class name
 */
public expect inline fun <reified T : Any> className(): String