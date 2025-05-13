package composegears.tiamat.example.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composegears.tiamat.*
import com.composegears.tiamat.compose.Navigation
import com.composegears.tiamat.compose.navigationPlatformDefault
import com.composegears.tiamat.compose.rememberNavController
import com.composegears.tiamat.navigation.NavController
import composegears.tiamat.example.content.content.HomeScreen
import composegears.tiamat.example.content.content.advanced.AdvBackStackAlteration
import composegears.tiamat.example.content.content.advanced.AdvDestinationsGraph
import composegears.tiamat.example.content.content.advanced.AdvExtensions
import composegears.tiamat.example.content.content.advanced.AdvSharedElementTransition
import composegears.tiamat.example.content.content.apr.APRFreeArgs
import composegears.tiamat.example.content.content.apr.APRNavArgs
import composegears.tiamat.example.content.content.apr.APRNavResult
import composegears.tiamat.example.content.content.architecture.ArchCustomSaveState
import composegears.tiamat.example.content.content.architecture.ArchViewModel
import composegears.tiamat.example.content.content.navigation.*
import composegears.tiamat.example.extra.A3rdParty
import composegears.tiamat.example.platform.Platform
import composegears.tiamat.example.platform.features
import composegears.tiamat.example.ui.core.AppTheme

@Composable
@Suppress("SpreadOperator")
fun App(
    navControllerConfig: NavController.() -> Unit = {},
    overlay: @Composable (navController: NavController) -> Unit = {},
) {
    AppTheme {
        Surface(Modifier.fillMaxSize()) {
            val rootNavController = rememberNavController(
                key = "rootNavController",
                startDestination = HomeScreen,
                configuration = navControllerConfig
            )
            Navigation(
                navController = rootNavController,
                destinations = arrayOf(
                    HomeScreen,
                    NavForwardAndBack,
                    NavReplace,
                    NavNested,
                    NavCustomAnimation,
                    NavTabs,
                    NavRoute,
                    APRNavArgs,
                    APRFreeArgs,
                    APRNavResult,
                    ArchViewModel,
                    ArchCustomSaveState,
                    AdvExtensions,
                    AdvBackStackAlteration,
                    AdvSharedElementTransition,
                    AdvDestinationsGraph,
                    *(Platform.features() + A3rdParty.features())
                        .map { it.destination }
                        .toTypedArray()
                ),
                modifier = Modifier.fillMaxSize(),
                contentTransformProvider = { navigationPlatformDefault(it) }
            )
            overlay(rootNavController)
        }
    }
}