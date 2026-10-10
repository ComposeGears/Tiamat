package com.composegears.tiamat

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavDestination
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry

fun createTestNavController(
    key: String? = null,
    saveable: Boolean = true,
    parent: NavController? = null,
    startDestination: NavDestination<*>? = null,
    backBehaviour: NavController.BackBehaviour = NavController.BackBehaviour.AllowUntilRoot,
) = NavController.create(
    key = key,
    saveable = saveable,
    parent = parent,
    startEntry = startDestination?.toNavEntry(),
    backBehaviour = backBehaviour,
    config = {}
)

/**
 * Extracts the first text value from a semantics node.
 * @return The text content, or null if no text is present.
 */
fun SemanticsNodeInteraction.readText(): String? = this
    .fetchSemanticsNode()
    .config
    .getOrNull(SemanticsProperties.Text)
    ?.firstOrNull()
    ?.text

/**
 * Runs the [action] on the UI thread and waits until the UI is idle.
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.act(action: () -> Unit) {
    runOnIdle(action)
    waitForIdle()
}
