package content.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.composegear.navigation.DeeplinkData
import com.composegears.tiamat.*
import content.examples.platform.MainPlatformScreen
import content.examples.platform.examples.AndroidViewLifecycleScreen
import content.examples.platform.examples.DeeplinkScreen
import content.examples.platform.examples.SavedStateScreen

actual val PlatformExample: NavDestination<Unit> by navDestination {

    val platformNavController = rememberNavController(
        key = "platformNavController",
        startDestination = MainPlatformScreen,
        destinations = arrayOf(
            MainPlatformScreen,
            SavedStateScreen,
            AndroidViewLifecycleScreen,
            DeeplinkScreen
        )
    )

    // pass deeplink deeper and handle inside screen
    val deeplink = freeArgs<DeeplinkData>()
    DisposableEffect(deeplink) {
        if (deeplink != null) {
            platformNavController.editBackStack {
                clear()
                add(MainPlatformScreen)
            }
            platformNavController.replace(
                dest = DeeplinkScreen,
                freeArgs = deeplink,
                transition = navigationNone()
            )
            clearFreeArgs() // clear deeplink to prevent double processing
        }
        onDispose { }
    }

    Navigation(
        navController = platformNavController,
        modifier = Modifier.fillMaxSize()
    )
}