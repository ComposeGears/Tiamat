package composegears.tiamat.example.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.composegears.tiamat.Navigation
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.rememberNavController
import composegears.tiamat.example.common.PlatformConfig.PlatformDestination
import composegears.tiamat.sample.koin.KoinIntegrationScreen

actual val PlatformExampleScreen by navDestination<Unit> {
    val platformNavController = rememberNavController(
        startDestination = PlatformScreen,
        destinations = platformConfig.destinations() + PlatformScreen
    )

    Navigation(
        navController = platformNavController,
        modifier = Modifier.fillMaxSize()
    )
}

actual val platformConfig = PlatformConfig(
    platformName = "Desktop",
    availableScreens = listOf(
        PlatformDestination(
            name = "Koin (ViewModel/SharedViewModel)",
            destination = KoinIntegrationScreen
        )
    )
)