package composegears.tiamat.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.NavDestination
import com.composegears.tiamat.NavDestinationScope
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import composegears.tiamat.example.ui.core.*

@Composable
private fun NavDestinationScope<*>.Screen(
    title: String,
    nextScreen: NavDestination<*>?,
) {
    val navController = navController()
    SimpleScreen(title) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (nextScreen != null) {
                NextButton(onClick = { navController.navigate(nextScreen) })
                Spacer()
            }
            BackButton(text = "Previous", onClick = navController::back)
            TextCaption(text = "Click button or press system `back` button to go back (ESC for desktop)")
            if (nextScreen == null) {
                Spacer()
                BackButton(
                    text = "To Screen1 inclusive",
                    onClick = {
                        navController.back(
                            to = SimpleForwardBackRootScreen1,
                            inclusive = true
                        )
                    }
                )
                TextCaption(text = "Navigate back to Screen1's parent")
            }
            Spacer()
            ExitButton(text = "Exit", onClick = { navController.back(to = MainScreen) })
            TextCaption(text = "Exit back to Main screen")
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
