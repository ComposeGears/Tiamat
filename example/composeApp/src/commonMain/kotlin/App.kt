import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.rememberNavController
import content.MainScreen
import content.examples.*

@Composable
fun App() {
    MaterialTheme {
        val rootNavController = rememberNavController(
            "rootNavController",
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
            )
        )
        Navigation(rootNavController, Modifier.fillMaxSize())
    }
}