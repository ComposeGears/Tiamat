package content.examples

import com.composegears.tiamat.NavDestination
import com.composegears.tiamat.navDestination
import content.examples.common.SimpleScreen
import content.examples.common.TextCaption

actual val PlatformExample: NavDestination<Unit> by navDestination {
    SimpleScreen("Platform iOs") {
        TextCaption("Nothing platform specific yet")
    }
}