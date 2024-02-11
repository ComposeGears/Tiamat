package content.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.composegears.tiamat.NavDestination
import com.composegears.tiamat.NavDestinationScope
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import content.MainScreen
import content.examples.common.SimpleScreen

@Composable
private fun NavDestinationScope<*>.Screen(
    title: String,
    nextScreen: NavDestination<*>?,
) {
    val navController = navController()
    SimpleScreen(title) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (nextScreen != null) {
                Button(onClick = { navController.navigate(nextScreen) }) {
                    Text("Go forward -> ")
                }
            }
            Text("Click button or press system `back` button to go back (ESC for desktop)")
            Button(onClick = navController::back) {
                Text(" <- Go back")
            }
            Text("Exit back to Main screen")
            Button(onClick = { navController.back(to = MainScreen) }) {
                Text(" <- Exit")
            }
        }
    }
}

val SimpleForwardBackRoot by navDestination<Unit> {
    Screen("Simple navigation: forward/back", SimpleForwardBackRootScreen1)
}

val SimpleForwardBackRootScreen1 by navDestination<Unit> {
    Screen("Simple navigation: forward/back (screen 1)", SimpleForwardBackRootScreen2)
}

val SimpleForwardBackRootScreen2 by navDestination<Unit> {
    Screen("Simple navigation: forward/back (screen 2)", SimpleForwardBackRootScreen3)
}

val SimpleForwardBackRootScreen3 by navDestination<Unit> {
    Screen("Simple navigation: forward/back (screen 3)", null)
}
