package composegears.tiamat.example.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.composegears.tiamat.*
import composegears.tiamat.example.DeeplinkData
import composegears.tiamat.example.common.PlatformConfig.PlatformDestination
import composegears.tiamat.example.platform.AndroidViewLifecycleScreen
import composegears.tiamat.example.platform.DeeplinkScreen
import composegears.tiamat.example.platform.SavedStateScreen
import composegears.tiamat.sample.koin.KoinIntegrationScreen

actual val platformConfig = PlatformConfig(
    platformName = "Android",
    availableScreens = listOf(
        PlatformDestination(
            name = "Android SavedState",
            destination = SavedStateScreen
        ),
        PlatformDestination(
            name = "AndroidView + Lifecycle handle",
            destination = AndroidViewLifecycleScreen
        ),
        PlatformDestination(
            name = "Deeplink",
            destination = DeeplinkScreen
        ),
        PlatformDestination(
            name = "Koin (ViewModel/SharedViewModel)",
            destination = KoinIntegrationScreen
        )
    )
)

actual val PlatformExampleScreen: NavDestination<Unit> by navDestination {
    val deeplink = freeArgs<DeeplinkData>()

    val platformNavController = rememberNavController(
        startDestination = PlatformScreen,
        destinations = platformConfig.destinations() + PlatformScreen
    ) {
        if (deeplink != null) {
            editBackStack {
                clear()
                add(PlatformScreen)
            }
            replace(
                dest = DeeplinkScreen,
                freeArgs = deeplink,
                transition = navigationNone()
            )
            clearFreeArgs()
        }
    }

    Navigation(
        navController = platformNavController,
        modifier = Modifier.fillMaxSize()
    )
}