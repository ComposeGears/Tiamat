package composegears.tiamat.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composegears.tiamat.NavController
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.StorageMode
import com.composegears.tiamat.rememberNavController
import composegears.tiamat.example.multimodule.MultiModuleRoot
import composegears.tiamat.example.ui.core.AppTheme

@Composable
@Suppress("SpreadOperator")
fun App(
    configure: @Composable (
        NavController,
        @Composable () -> Unit
    ) -> Unit = { _, content ->
        content()
    }
) {
    AppTheme {
        Surface {
            val rootNavController = rememberNavController(
                key = "rootNavController",
                storageMode = StorageMode.ResetOnDataLoss,
                startDestination = MainScreen,
                destinations = arrayOf(
                    MainScreen,
                    SimpleForwardBackRoot,
                    SimpleForwardBackRootScreen1,
                    SimpleForwardBackRootScreen2,
                    SimpleForwardBackRootScreen3,
                    SimpleReplaceRoot,
                    SimpleReplaceScreen1,
                    SimpleReplaceScreen2,
                    SimpleReplaceScreen3,
                    SimpleTabsRoot,
                    NestedNavigationRoot,
                    DataPassingParamsRoot,
                    DataPassingParamsScreen,
                    DataPassingFreeArgsRoot,
                    DataPassingFreeArgsScreen,
                    DataPassingResultRoot,
                    DataPassingResultScreen,
                    ViewModelsRoot,
                    CustomTransitionRoot,
                    CustomTransitionScreen1,
                    CustomTransitionScreen2,
                    MultiModuleRoot,
                    BackStackAlterationRoot,
                    TwoPaneResizableRoot,
                    PlatformExamplesScreen,
                    *platformExamplesConfig.destinations()
                )
            )
            configure(rootNavController) {
                Navigation(
                    navController = rootNavController,
                    modifier = Modifier.fillMaxSize().systemBarsPadding()
                )
            }
        }
    }
}