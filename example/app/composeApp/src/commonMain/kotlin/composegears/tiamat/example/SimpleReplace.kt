package composegears.tiamat.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
    nextScreen: NavDestination<*>?
) {
    val navController = navController()
    SimpleScreen(title) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (nextScreen != null) {
                NextButton("Replace with next", onClick = { navController.replace(nextScreen) })
                Spacer(Modifier.height(16.dp))
            }
            BackButton(onClick = navController::back)
            TextCaption("Click button or press system `back` button to go back (ESC for desktop)")
        }
    }
}

val SimpleReplaceRoot by navDestination<Unit>(webPathExtension()) {
    val navController = navController()
    SimpleScreen("Simple navigation: Replace") {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { navController.navigate(SimpleReplaceScreen1) }) {
                Text("Go to 1st")
            }
            TextCaption(text = "The screens next from this will replace each other")
            TextCaption(text = "Clicking `back` on either of them will back to this one")
        }
    }
}

// In case some of your screens made `Loop` there will be known type-checking-recursive-problems
// you may read about it here: https://youtrack.jetbrains.com/issue/KT-10716/Type-checking-recursive-problems
// Simple solution is to explicitly declare nav destination type (  val Screen : NavDestination<Params> by ...)
val SimpleReplaceScreen1: NavDestination<Unit> by navDestination {
    Screen("Simple navigation: Replace (Screen 1)", SimpleReplaceScreen2)
}

val SimpleReplaceScreen2 by navDestination<Unit> {
    Screen("Simple navigation: Replace (Screen 2)", SimpleReplaceScreen3)
}

val SimpleReplaceScreen3 by navDestination<Unit> {
    Screen("Simple navigation: Replace (Screen 3)", SimpleReplaceScreen1)
}