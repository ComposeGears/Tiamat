package content.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.composegears.tiamat.NavDestination
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.rememberNavController
import content.examples.platform.examples.AndroidViewLifecycleScreen
import content.examples.platform.MainPlatformScreen
import content.examples.platform.examples.SavedStateScreen

actual val PlatformExample: NavDestination<Unit> by navDestination {
    val platformNavController = rememberNavController(
        key = "platformNavController",
        startDestination = MainPlatformScreen,
        destinations = arrayOf(
            MainPlatformScreen,
            SavedStateScreen,
            AndroidViewLifecycleScreen,
        )
    )
    Navigation(
        navController = platformNavController,
        modifier = Modifier.fillMaxSize()
    )
}