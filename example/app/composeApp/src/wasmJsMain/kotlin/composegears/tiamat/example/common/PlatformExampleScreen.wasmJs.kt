package composegears.tiamat.example.common

import com.composegears.tiamat.NavDestination
import com.composegears.tiamat.navDestination
import composegears.tiamat.example.ui.core.SimpleScreen
import composegears.tiamat.example.ui.core.TextCaption

actual val platformConfig = PlatformConfig(
    platformName = "Wasm",
    availableScreens = emptyList()
)

actual val PlatformExampleScreen: NavDestination<Unit> by navDestination {
    SimpleScreen("Platform ${platformConfig.platformName}") {
        TextCaption("Nothing platform specific yet")
    }
}
