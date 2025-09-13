package com.composegears.tiamat.compose

import androidx.compose.runtime.Composable
import com.composegears.tiamat.navigation.NavDestination
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry

/**
 * A utility composable for previewing a navigation destination in a Compose Preview.

 * @param destination The navigation destination to wrap for preview rendering.
 * @param navArgs Optional navigation arguments to pass to the destination.
 * @param freeArgs Optional free-form arguments that can be passed to the destination.
 * @param navResult Optional navigation result data that would normally be received from
 *                  a previous destination in the navigation stack.
 *
 * Example simple usage:
 *
 * ```kotlin
 * val DemoScreen by navDestination {
 *     // Your screen content here
 * }
 *
 * @Preview
 * @Composable
 * private fun DemoScreenPreview() {
 *     TiamatDestinationPreview(destination = DemoScreen)
 * }
 * ```
 *
 * Example with arguments:

 * ```kotlin
 *
 * data class SomeArgs(val id: Int)
 *
 * val DemoScreenWithArgs by navDestination<SomeArgs> {
 *     val args = navArgs()
 *     Text("id=${args.id}")
 * }
 *
 * @Preview
 * @Composable
 * private fun DemoScreenWithArgsPreview() {
 *     TiamatDestinationPreview(
 *         destination = DemoScreenWithArgs,
 *         navArgs = SomeArgs(id = 123),
 *     )
 * }
 * ```
 *
 * @see NavDestination
 */
@Composable
public fun <T : Any> TiamatDestinationPreview(
    destination: NavDestination<T>,
    navArgs: T? = null,
    freeArgs: Any? = null,
    navResult: Any? = null
) {
    Navigation(
        navController = rememberNavController(
            startEntry = destination.toNavEntry(
                navArgs = navArgs,
                freeArgs = freeArgs,
                navResult = navResult
            ),
        ),
        destinations = arrayOf(destination)
    )
}
