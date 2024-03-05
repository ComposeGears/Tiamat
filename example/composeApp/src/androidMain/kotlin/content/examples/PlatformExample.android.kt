package content.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.composegears.tiamat.*
import content.examples.model.DeeplinkData
import content.examples.platform.MainPlatformScreen
import content.examples.platform.examples.AndroidViewLifecycleScreen
import content.examples.platform.examples.DeeplinkScreen
import content.examples.platform.examples.SavedStateScreen

actual val PlatformExample: NavDestination<Unit> by navDestination {
    val deeplinkData = freeArgs<DeeplinkData>()

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

    LaunchedEffect(deeplinkData) {
        deeplinkData ?: return@LaunchedEffect
        if (deeplinkData != null) {
            with(platformNavController) {
                editBackStack {
                    clear()
                    add(MainPlatformScreen)
                }
                replace(dest = DeeplinkScreen, freeArgs = deeplinkData)
            }
        }
    }

    Navigation(
        navController = platformNavController,
        modifier = Modifier.fillMaxSize()
    )
}