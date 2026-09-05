package com.composegears.tiamat.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavDestination

private val LocalOverlayNavController = staticCompositionLocalOf<NavController?> { null }

/**
 * A [ContentExtension] that adds a local overlay host above a destination's main content.
 *
 * Attach the same instance to the destinations that should participate in the overlay flow.
 * Inside destination content, use `ext<OverlaysExtension<*>>()?.overlayNavController()` to access
 * the local overlay [NavController] and open overlay destinations.
 */
public class OverlaysExtension(
    private val destinationLoader: DestinationLoader,
    private val handleSystemBackEvents: Boolean = true,
    private val overlaysNavControllerFactory: (@Composable () -> NavController)? = null,
) : ContentExtension<Any> {

    public constructor(
        destinations: Array<NavDestination<*>>,
        handleSystemBackEvents: Boolean = true,
        overlaysNavControllerFactory: (@Composable () -> NavController)? = null
    ) : this(
        destinationLoader = DestinationLoader.from(destinations),
        handleSystemBackEvents = handleSystemBackEvents,
        overlaysNavControllerFactory = overlaysNavControllerFactory,
    )

    /**
     * Returns the overlay [NavController] stored in the local composition created by this extension.
     */
    @Composable
    public fun overlayNavController(): NavController =
        LocalOverlayNavController.current ?: error(
            "OverlaysExtension is not attached to the current composition"
        )

    @Composable
    override fun NavDestinationScope<out Any>.Content(
        entryContent: @Composable () -> Unit,
    ) {
        val overlayNavController = overlaysNavControllerFactory
            ?.invoke()
            ?: rememberNavController(
                key = "OverlaysExtensionNavController",
                saveable = true,
            )

        CompositionLocalProvider(LocalOverlayNavController provides overlayNavController) {
            Box {
                entryContent()
                NavigationScene(
                    navController = overlayNavController,
                    destinationLoader = destinationLoader,
                    handleSystemBackEvent = handleSystemBackEvents,
                ) {
                    val stack = overlayNavController.navStackAsState().value
                    Box {
                        for (entry in stack) {
                            EntryContent(entry)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Navigates back in the overlay navigation stack if possible, or clears the overlay stack if not.
 */
public fun NavController.overlayBack() {
    if (canNavigateBack()) back()
    else editNavStack { emptyList() }
}