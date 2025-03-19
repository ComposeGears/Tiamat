package composegears.tiamat.example.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composegears.tiamat.*
import com.composegears.tiamat.destinations.InstallIn
import com.composegears.tiamat.destinations.TiamatDestinations
import composegears.tiamat.example.content.content.HomeScreen
import composegears.tiamat.example.content.content.advanced.AdvBackStackAlteration
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
import kotlin.reflect.KClass

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
                storageMode = StorageMode.Memory,
                startDestination = HomeScreen,
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
                    *(Platform.features() + A3rdParty.features())
                        .map { it.destination }
                        .toTypedArray()
                ),
                configuration = navControllerConfig
            )
            Navigation(
                navController = rootNavController,
                modifier = Modifier.fillMaxSize(),
                contentTransformProvider = { navigationPlatformDefault(it) }
            )
            overlay(rootNavController)
        }
    }
}


object TestNavControllerDestinations : TiamatDestinations

@InstallIn(TestNavControllerDestinations::class)
val Screen1 by navDestination<Unit> { }

@InstallIn(TestNavControllerDestinations::class)
val Screen2 = NavDestination<Unit>(name = "Screen2", extensions = emptyList()) {}

@InstallIn(TestNavControllerDestinations::class)
object Screen3 : NavDestination<Int> {
    override val name: String = "Screen3"
    override val extensions: List<Extension<Int>> = emptyList()

    @Composable
    override fun NavDestinationScope<Int>.Content() {
    }
}

class Screen4 : NavDestination<Int> {
    override val name: String = "Screen4"
    override val extensions: List<Extension<Int>> = emptyList()

    @Composable
    override fun NavDestinationScope<Int>.Content() {
    }
}

@InstallIn(TestNavControllerDestinations::class)
val Screen5 = Screen4()

@Composable
fun Test() {
    val navController = rememberNavController(
        key = "testNavController",
        storageMode = StorageMode.Memory,
        startDestination = Screen1,
        destinations = TestNavControllerDestinations.items()
    )

    Navigation(
        navController = navController,
        modifier = Modifier.fillMaxSize(),
    )
}