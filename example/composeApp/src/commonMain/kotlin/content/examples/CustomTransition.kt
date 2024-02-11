package content.examples

import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.navigationSlideInOut
import com.composegears.tiamat.navigationSlideOutToBottom
import content.examples.common.SimpleScreen

//Text("Go forward -> ")
//Text(" <- Go back")


val CustomTransitionRoot by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("Custom transition") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Open Screen1 with `slideIn` animation")
            Button({
                navController.navigate(
                    CustomTransitionScreen1,
                    transition = navigationSlideInOut(true)
                )
            }) {
                Text("Go forward -> ")
            }
        }
    }
}

val CustomTransitionScreen1 by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("Custom transition - Screen 1", MaterialTheme.colors.secondary) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Open Screen2 with custom animation")
            Button({
                navController.navigate(
                    CustomTransitionScreen2,
                    transition = expandIn(expandFrom = Alignment.Center) togetherWith
                            shrinkOut(shrinkTowards = Alignment.Center)
                )
            }) {
                Text("Go forward -> ")
            }
            // override default `back` transition from this screen
            // will be used for 'system back action'
            // and manual back call (if not transition specified)
            LaunchedEffect(Unit) {
                navController.setPendingBackTransition(
                    navigationSlideInOut(false)
                )
            }
            Text("\nGo back `slideOut` animation")
            Button({ navController.back() }) {
                Text(" <- Go back")
            }
        }
    }
}

val CustomTransitionScreen2 by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("Custom transition - Screen 2", MaterialTheme.colors.secondaryVariant) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LaunchedEffect(Unit) {
                navController.setPendingBackTransition(
                    expandIn(expandFrom = Alignment.Center) togetherWith
                            shrinkOut(shrinkTowards = Alignment.Center)
                )
            }
            Text("Clicking `system back` or this button will use shrink+expand animation")
            Button({ navController.back() }) {
                Text(" <- Go back")
            }
            Text("\n\nClicking this button will override and use slide-to-bottom animation")
            Button({ navController.back(transition = navigationSlideOutToBottom()) }) {
                Text(" <- Go back")
            }
        }
    }
}