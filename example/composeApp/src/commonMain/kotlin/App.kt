import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.StorageMode
import com.composegears.tiamat.rememberNavController
import content.MainScreen
import content.examples.*
import content.examples.model.DeeplinkData

@Composable
fun App(
    deeplinkData: DeeplinkData? = null,
    onDeeplinkHandled: () -> Unit = {}
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
                    ViewModelsScreen,
                    CustomTransitionRoot,
                    CustomTransitionScreen1,
                    CustomTransitionScreen2,
                    BackStackAlterationRoot,
                    PlatformExample
                )
            )

            LaunchedEffect(deeplinkData) {
                deeplinkData ?: return@LaunchedEffect

                with(rootNavController) {
                    editBackStack {
                        clear()
                        add(MainScreen)
                    }
                    replace(dest = PlatformExample, freeArgs = deeplinkData)
                }
                onDeeplinkHandled()
            }

            Navigation(
                navController = rootNavController,
                modifier = Modifier.fillMaxSize().systemBarsPadding()
            )
        }
    }
}