package com.composegears.tiamat.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.destinations.TiamatGraph
import com.composegears.tiamat.navigation.NavDestination
import kotlin.test.Test

@OptIn(TiamatExperimentalApi::class)
class TiamatDestinationsComposeTest {

    companion object {
        val Screen by navDestination {
            Box(Modifier.testTag("ScreenContent"))
        }
    }

    object Graph : TiamatGraph {
        override fun destinations(): Array<NavDestination<*>> = arrayOf(Screen)
    }

    class TestLifecycleOwner : LifecycleOwner {
        override val lifecycle = LifecycleRegistry(this)

        init {
            lifecycle.currentState = Lifecycle.State.STARTED
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun `Navigation # displays content when used with TiamatGraph`() = runComposeUiTest {
        val testLifecycle = TestLifecycleOwner()
        setContent {
            CompositionLocalProvider(
                LocalLifecycleOwner provides testLifecycle
            ) {
                val nc = rememberNavController(
                    startDestination = Screen,
                )
                Navigation(
                    navController = nc,
                    graph = Graph
                )
            }
        }
        onNodeWithTag("ScreenContent").assertExists()
    }
}