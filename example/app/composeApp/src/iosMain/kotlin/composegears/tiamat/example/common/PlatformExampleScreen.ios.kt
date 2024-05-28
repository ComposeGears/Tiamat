package composegears.tiamat.example.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.composegears.tiamat.NavDestination
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.rememberNavController
import composegears.tiamat.sample.koin.KoinIntegrationScreen

actual val platformConfig = PlatformConfig(
    platformName = "iOS",
    availableScreens = listOf(
        PlatformConfig.PlatformDestination(
            name = "Koin (ViewModel/SharedViewModel)",
            destination = KoinIntegrationScreen
        )
    )
)

actual val PlatformExampleScreen: NavDestination<Unit> by navDestination {
    val platformNavController = rememberNavController(
        startDestination = PlatformScreen,
        destinations = platformConfig.destinations() + PlatformScreen
    )
    Navigation(
        navController = platformNavController,
        modifier = Modifier.fillMaxSize()
    )
}