package composegears.tiamat.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composegears.tiamat.compose.Navigation
import com.composegears.tiamat.compose.navigationPlatformDefault
import com.composegears.tiamat.compose.rememberNavController
import com.composegears.tiamat.navigation.NavController
import composegears.tiamat.sample.content.HomeScreen
import composegears.tiamat.sample.content.advanced.*
import composegears.tiamat.sample.content.apr.APRFreeArgs
import composegears.tiamat.sample.content.apr.APRNavArgs
import composegears.tiamat.sample.content.apr.APRNavResult
import composegears.tiamat.sample.content.architecture.ArchCustomSaveState
import composegears.tiamat.sample.content.architecture.ArchSerializableData
import composegears.tiamat.sample.content.architecture.ArchViewModel
import composegears.tiamat.sample.content.navigation.*
import composegears.tiamat.sample.platform.Platform
import composegears.tiamat.sample.platform.features
import composegears.tiamat.sample.ui.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

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
                    ArchSerializableData,
                    AdvExtensions,
                    AdvOverlayDestinations,
                    AdvBackStackAlteration,
                    AdvSharedElementTransition,
                    AdvDestinationsGraph,
                    AdvTwoPane,
                    AdvAdaptiveListDetails,
                    *Platform.features()
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

@Preview
@Composable
private fun AppPreview() {
    App()
}
