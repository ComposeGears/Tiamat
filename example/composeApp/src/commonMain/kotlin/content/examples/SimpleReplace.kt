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
import content.examples.common.SimpleScreen

@Composable
private fun NavDestinationScope<*>.Screen(
    title: String,
    nextScreen: NavDestination<*>?
) {
    val navController = navController()
    SimpleScreen(title) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (nextScreen != null) {
                Button(onClick = { navController.replace(nextScreen) }) {
                    Text("Replace with next -> ")
                }
            }
            Text("Click button or press system `back` button to go back (ESC for desktop)")
            Button(onClick = navController::back) {
                Text(" <- Go back")
            }
        }
    }
}

val SimpleReplaceRoot by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("Simple navigation: Replace") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("The screens next from this will replace each other")
            Text("Clicking `back` on either of them will back to this one")
            Button(onClick = { navController.navigate(SimpleReplaceScreen1) }) {
                Text("Go to 1st")
            }
        }
    }
}

val SimpleReplaceScreen1 by navDestination<Unit> {
    Screen("Simple navigation: Replace (Screen 1)", SimpleReplaceScreen2)
}

val SimpleReplaceScreen2 by navDestination<Unit> {
    Screen("Simple navigation: Replace (Screen 2)", SimpleReplaceScreen3)
}

val SimpleReplaceScreen3 by navDestination<Unit> {
    Screen("Simple navigation: Replace (Screen 3)", null)
}

