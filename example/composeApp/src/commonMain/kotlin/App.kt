import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.StorageMode
import com.composegears.tiamat.rememberNavController
import content.MainScreen
import content.examples.*

@Composable
fun App() {
    AppTheme {
        Surface {
            val rootNavController = rememberNavController(
                key = "rootNavController",
                storageMode = StorageMode.DataStore.ResetOnDataLoss,
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
                    DataPassingResultRoot,
                    DataPassingResultScreen,
                    ViewModelsRoot,
                    ViewModelsScreen,
                    CustomTransitionRoot,
                    CustomTransitionScreen1,
                    CustomTransitionScreen2,
                    PlatformExample
                )
            )
            Navigation(
                navController = rootNavController,
                modifier = Modifier.fillMaxSize().systemBarsPadding()
            )
        }
    }
}