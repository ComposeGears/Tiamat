package composegears.tiamat.sample.content

import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.navigation.NavDestination
import composegears.tiamat.sample.ui.ScreenInfo

@Suppress("TopLevelPropertyNaming")
internal const val TestScreenEnabled = false

val InternalTestScreen: NavDestination<Unit> by navDestination(
    ScreenInfo("Internal test")
) {
    // testing content template
}