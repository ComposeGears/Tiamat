package composegears.tiamat.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import composegears.tiamat.example.content.App
import composegears.tiamat.example.ui.core.LocalThemeConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
            val themeConfig = LocalThemeConfig.current
            LaunchedEffect(themeConfig.isDarkMode) {
                // todo check colors
                if (themeConfig.isDarkMode) enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                    navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
                ) else enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.BLACK),
                    navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.BLACK)
                )
            }
        }
    }
}