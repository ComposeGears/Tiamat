package content.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.composegears.tiamat.NavDestination
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.rememberNavController
import content.examples.common.SimpleScreen
import content.examples.common.TextCaption
import content.examples.koin.KoinIntegration

actual val PlatformExample: NavDestination<Unit> by navDestination {
    val platformNavController = rememberNavController(
        key = "platformNavController",
        startDestination = MainPlatformScreen,
        destinations = arrayOf(MainPlatformScreen, KoinIntegration)
    )
    Navigation(
        navController = platformNavController,
        modifier = Modifier.fillMaxSize()
    )
}