package composegears.tiamat.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.navigation.Route
import composegears.tiamat.app.platform.CameraXLifecycleScreen
import composegears.tiamat.app.platform.HiltSample
import composegears.tiamat.app.platform.PredictiveBack
import composegears.tiamat.sample.App
import composegears.tiamat.sample.PlatformFeatures
import composegears.tiamat.sample.content.HomeScreen
import composegears.tiamat.sample.ui.AppFeature
import composegears.tiamat.sample.ui.LocalThemeConfig
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var deeplinkIntent by mutableStateOf<Intent?>(null)
    private val platformFeatures = PlatformFeatures(
        platformName = "Android",
        features = listOf(
            AppFeature(
                name = "CameraX",
                description = "CameraX + Lifecycle",
                destination = CameraXLifecycleScreen
            ),
            AppFeature(
                name = "Predictive back",
                description = "Android predictive back",
                destination = PredictiveBack
            ),
            AppFeature(
                name = "Hilt",
                description = "Hilt integration sample",
                destination = HiltSample
            )
        )
    )

    @OptIn(TiamatExperimentalApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App(platformFeatures) { navController ->
                // deeplink handler
                LaunchedEffect(deeplinkIntent) {
                    val data = deeplinkIntent?.data
                    if (data != null) {
                        // process deeplink, eg: parse and use Route Api -> navController.route(...)
                        // for now it will reopen HomeScreen on any intent
                        navController.route(
                            Route {
                                element(HomeScreen)
                            }
                        )
                    }
                    deeplinkIntent = null
                }
            }
            // theme config handler
            val themeConfig = LocalThemeConfig.current
            LaunchedEffect(themeConfig.isDarkMode) {
                if (themeConfig.isDarkMode) enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                    navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
                ) else enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.BLACK),
                    navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.BLACK)
                )
            }
        }
        deeplinkIntent = intent
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deeplinkIntent = intent
    }
}