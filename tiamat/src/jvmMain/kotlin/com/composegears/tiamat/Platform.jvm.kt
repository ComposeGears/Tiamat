package com.composegears.tiamat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf

val LocalNavBackHandler = staticCompositionLocalOf { NavBackHandler() }

/**
 * Global in-memory data storage
 */
private val globalDataStorage: NavControllersStorage = NavControllersStorage()

@Composable
internal actual fun rootNavControllersStore(): NavControllersStorage = globalDataStorage

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
 * Platform provided system back handler
 */
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

/**
 * We can not call T::class in @Composable functions,
 *
 * workaround is to call it outside of @Composable via regular inline fun
 */
actual inline fun <reified T : Any> className(): String = T::class.qualifiedName!!