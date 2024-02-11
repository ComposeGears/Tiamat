package content.examples

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import content.examples.common.SimpleScreen

@Composable
private fun NavDestinationScope<*>.Screen(
    title: String,
) {
    val navController = navController()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title)
            // custom back action
            val backAction: () -> Unit = remember {
                {
                    // if there is last screen (empty backstack) and it is not a 1st tab
                    // then we open 1st tab, else just perform regular back
                    // having this we will always come back to 1st tab before exit tabs screen
                    if (!navController.canGoBack() && navController.current != SimpleTabsTab1) {
                        navController.replace(SimpleTabsTab1)
                    } else {
                        navController.back()
                    }
                }
            }
            // we can go back if there is smth in backstack or if it is not first tab
            val canGoBack by remember {
                derivedStateOf { // prevent extra recompositions
                    navController.canGoBack() || navController.current != SimpleTabsTab1
                }
            }
            NavBackHandler(canGoBack, backAction)
            Button(onClick = backAction) {
                Text(" <- Go back")
            }
        }
    }
}

val SimpleTabsRoot by navDestination<Unit> {
    SimpleScreen("BottomBar Tabs + custom back") {
        val tabNavController = rememberNavController(
            "tabs",
            startDestination = SimpleTabsTab1,
            destinations = arrayOf(
                SimpleTabsTab1,
                SimpleTabsTab2,
                SimpleTabsTab3,
            )
        )
        Column(Modifier.fillMaxSize()) {
            Navigation(
                tabNavController,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                handleSystemBackEvent = false
            )
            val tabs = remember {
                listOf(
                    SimpleTabsTab1,
                    SimpleTabsTab2,
                    SimpleTabsTab3,
                )
            }
            Divider()
            Row {
                tabs.onEach {
                    TextButton(
                        onClick = { tabNavController.popToTop(it) },
                        enabled = it != tabNavController.current,
                        modifier = Modifier.weight(1f).height(64.dp)
                    ) {
                        Text(it.name + (if (it == tabNavController.current) "•" else ""))
                    }
                }
            }
        }
    }
}

val SimpleTabsTab1 by navDestination<Unit> { Screen("Tab1") }
val SimpleTabsTab2 by navDestination<Unit> { Screen("Tab2") }
val SimpleTabsTab3 by navDestination<Unit> { Screen("Tab3") }