package composegears.tiamat.example

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
import com.composegears.tiamat.Route
import com.composegears.tiamat.TiamatExperimentalApi
import composegears.tiamat.example.content.App
import composegears.tiamat.example.content.content.HomeScreen
import composegears.tiamat.example.ui.core.LocalThemeConfig

class MainActivity : ComponentActivity() {

    private var deeplinkIntent by mutableStateOf<Intent?>(null)

    @OptIn(TiamatExperimentalApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App { navController ->
                // deeplink handler
                LaunchedEffect(deeplinkIntent) {
                    val data = deeplinkIntent?.data
                    if (data != null) {
                        // process deeplink, eg: parse and use Route Api -> navController.route(...)
                        // for now it will reopen HomeScreen on any intent
                        navController.route(Route.build(HomeScreen))
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