package composegears.tiamat.example.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.StorageMode
import com.composegears.tiamat.rememberNavController
import composegears.tiamat.example.content.content.HomeScreen
import composegears.tiamat.example.content.content.StubScreen
import composegears.tiamat.example.ui.core.AppTheme

@Composable
@Suppress("SpreadOperator")
fun App() {
    AppTheme {
        Surface(Modifier.fillMaxSize()) {
            val rootNavController = rememberNavController(
                key = "rootNavController",
                storageMode = StorageMode.Memory,
                startDestination = HomeScreen,
                destinations = arrayOf(
                    HomeScreen,
                    StubScreen
                )
            )
            Navigation(rootNavController, Modifier.fillMaxSize())
        }
    }
}