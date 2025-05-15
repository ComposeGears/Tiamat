package com.composegears.tiamat.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.destinations.TiamatGraph
import com.composegears.tiamat.navigation.NavDestination
import kotlin.test.Test


@OptIn(TiamatExperimentalApi::class)
class TiamatDestinationsComposeTest {

    object Screen : ComposeNavDestination<Unit> {
        override val name: String = "Screen"
        override val extensions: List<NavExtension<Unit>> = emptyList()

        @Composable
        override fun NavDestinationScope<Unit>.Content() {
            Box(Modifier.testTag("ScreenContent"))
        }
    }

    object Graph : TiamatGraph {
        override fun destinations(): Array<NavDestination<*>> = arrayOf(Screen)
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun `Navigation displays content when used with TiamatGraph`() = runComposeUiTest {
        setContent {
            val nc = rememberNavController(
                startDestination = Screen,
            )
            Navigation(
                navController = nc,
                graph = Graph
            )
        }
        onNodeWithTag("ScreenContent").assertExists()
    }
}