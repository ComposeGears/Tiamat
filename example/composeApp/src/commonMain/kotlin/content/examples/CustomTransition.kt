package content.examples

import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import com.composegears.tiamat.navigationSlideInOut
import com.composegears.tiamat.navigationSlideOutToBottom
import content.examples.common.*

val CustomTransitionRoot by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("Custom transition") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NextButton(onClick = {
                navController.navigate(
                    CustomTransitionScreen1,
                    transition = navigationSlideInOut(true)
                )
            })
            TextCaption("Open Screen1 with `slideIn` animation")
        }
    }
}

val CustomTransitionScreen1 by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("Custom transition - Screen 1", MaterialTheme.colorScheme.secondary) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NextButton(onClick = {
                navController.navigate(
                    CustomTransitionScreen2,
                    transition = expandIn(expandFrom = Alignment.Center) togetherWith
                            shrinkOut(shrinkTowards = Alignment.Center)
                )
            })
            TextCaption("Open Screen2 with custom animation")
            // override default `back` transition from this screen
            // will be used for 'system back action'
            // and manual back call (if not transition specified)
            LaunchedEffect(Unit) {
                navController.setPendingBackTransition(
                    navigationSlideInOut(false)
                )
            }
            Spacer()
            BackButton(onClick = navController::back)
            TextCaption("Go back `slideOut` animation")
        }
    }
}

val CustomTransitionScreen2 by navDestination<Unit> {
    val navController = navController()
    SimpleScreen("Custom transition - Screen 2", MaterialTheme.colorScheme.tertiary) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LaunchedEffect(Unit) {
                navController.setPendingBackTransition(
                    expandIn(expandFrom = Alignment.Center) togetherWith
                            shrinkOut(shrinkTowards = Alignment.Center)
                )
            }
            BackButton(onClick = navController::back)
            TextCaption("Clicking `system back` or this button will use shrink+expand animation")

            Spacer(32.dp)
            BackButton(onClick = { navController.back(transition = navigationSlideOutToBottom()) })
            TextCaption("Clicking this button will override and use slide-to-bottom animation")
        }
    }
}