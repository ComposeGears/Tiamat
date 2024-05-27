package content.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.composegear.navigation.DeeplinkData
import com.composegears.tiamat.*
import content.examples.koin.KoinIntegration
import content.examples.platform.MainPlatformScreen
import content.examples.platform.examples.AndroidViewLifecycleScreen
import content.examples.platform.examples.DeeplinkScreen
import content.examples.platform.examples.SavedStateScreen

actual val PlatformExample: NavDestination<Unit> by navDestination {
    val deeplink = freeArgs<DeeplinkData>()

    val platformNavController = rememberNavController(
        key = "platformNavController",
        startDestination = MainPlatformScreen,
        destinations = arrayOf(
            MainPlatformScreen,
            SavedStateScreen,
            AndroidViewLifecycleScreen,
            DeeplinkScreen,
            KoinIntegration
        )
    ) {
        if (deeplink != null) {
            editBackStack {
                clear()
                add(MainPlatformScreen)
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