package content.examples

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import content.examples.common.BackButton
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(title)
            // custom back action
            val backAction: () -> Unit = remember {
                {
                    // if there is last screen (empty backstack) and it is not a 1st tab
                    // then we open 1st tab, else just perform regular back
                    // having this we will always come back to 1st tab before exit tabs screen
                    if (!navController.canGoBack() && navController.current != Tab1) {
                        navController.replace(Tab1)
                    } else {
                        navController.back()
                    }
                }
            }
            // we can go back if there is smth in backstack or if it is not first tab
            val canGoBack by remember {
                derivedStateOf { // prevent extra recompositions
                    navController.canGoBack() || navController.current != Tab1
                }
            }
            NavBackHandler(canGoBack, backAction)
            BackButton(onClick = backAction)
        }
    }
}

val SimpleTabsRoot by navDestination<Unit> {
    SimpleScreen("BottomBar Tabs + custom back") {
        val tabNavController = rememberNavController(
            key = "tabs",
            startDestination = Tab1,
            destinations = arrayOf(Tab1, Tab2, Tab3)
        )
        Column(Modifier.fillMaxSize()) {
            Navigation(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                navController = tabNavController,
                handleSystemBackEvent = false
            )
            val tabs = remember { listOf(Tab1, Tab2, Tab3) }
            NavigationBar {
                tabs.onEach {
                    NavigationBarItem(
                        onClick = { tabNavController.popToTop(it) },
                        selected = it == tabNavController.current,
                        icon = { Icon(Icons.Default.ChangeHistory, "") },
                        label = { Text(it.name) }
                    )
                }
            }
        }
    }
}

val Tab1 by navDestination<Unit> { Screen("Tab1") }
val Tab2 by navDestination<Unit> { Screen("Tab2") }
val Tab3 by navDestination<Unit> { Screen("Tab3") }