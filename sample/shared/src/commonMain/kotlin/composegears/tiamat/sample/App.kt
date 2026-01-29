package composegears.tiamat.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.composegears.tiamat.compose.Navigation
import com.composegears.tiamat.compose.navigationPlatformDefault
import com.composegears.tiamat.compose.rememberNavController
import com.composegears.tiamat.navigation.NavController
import composegears.tiamat.sample.content.HomeItems
import composegears.tiamat.sample.content.HomeScreen
import composegears.tiamat.sample.ui.AppFeature
import composegears.tiamat.sample.ui.AppTheme

internal val LocalPlatformFeatures = staticCompositionLocalOf<PlatformFeatures?> { null }

@Composable
@Suppress("SpreadOperator")
fun App(
    platformFeatures: PlatformFeatures? = null,
    navControllerConfig: NavController.() -> Unit = {},
    overlay: @Composable (navController: NavController) -> Unit = {},
) {
    CompositionLocalProvider(LocalPlatformFeatures provides platformFeatures) {
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
                        *HomeItems.flatMap { it.items }.map { it.destination }.toTypedArray(),
                        *platformFeatures?.features?.map { it.destination }?.toTypedArray() ?: emptyArray()
                    ),
                    modifier = Modifier.fillMaxSize(),
                    contentTransformProvider = { navigationPlatformDefault(it) }
                )
                overlay(rootNavController)
            }
        }
    }
}

data class PlatformFeatures(
    val platformName: String,
    val features: List<AppFeature>
)

@Preview
@Composable
private fun AppPreview() {
    App()
}
