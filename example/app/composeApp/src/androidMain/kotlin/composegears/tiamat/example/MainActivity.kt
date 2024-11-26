package composegears.tiamat.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import composegears.tiamat.example.content.App
import composegears.tiamat.example.ui.core.LocalThemeConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
            val view = LocalView.current
            val themeConfig = LocalThemeConfig.current
            LaunchedEffect(themeConfig.isDarkMode) {
                // todo call enableEdgeToEdge instead
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !themeConfig.isDarkMode
            }
        }
    }
}