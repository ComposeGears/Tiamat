package composegears.tiamat.example

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
import composegears.tiamat.example.ui.core.BackButton
import composegears.tiamat.example.ui.core.NextButton
import composegears.tiamat.example.ui.core.SimpleScreen
import composegears.tiamat.example.ui.core.webPathExtension

@Composable
private fun Screen(
    title: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(title)
            val tabContentNavController = rememberNavController(
                key = "${title}NavController",
                startDestination = TabScreen1,
                destinations = arrayOf(
                    TabScreen1,
                    TabScreen2,
                    TabScreen3,
                )
            )
            Navigation(
                navController = tabContentNavController,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun NavDestinationScope<*>.TabScreen(
    title: String,
    next: NavDestination<*>?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val navController = navController()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(title)
            if (next != null) NextButton { navController.navigate(next) }
            BackButton { navController.back() }
        }
    }
}

val SimpleTabsRoot by navDestination<Unit>(webPathExtension()) {
    SimpleScreen("BottomBar Tabs + custom back") {
        val tabNavController = rememberNavController(
            key = "tabs",
            startDestination = Tab1,
            destinations = arrayOf(Tab1, Tab2, Tab3)
        )
        Column(Modifier.fillMaxSize()) {
            // custom back action
            val backAction: () -> Unit = remember {
                {
                    // if there is last screen (empty backstack) and it is not a 1st tab
                    // then we open 1st tab, else just perform regular back
                    // having this we will always come back to 1st tab before exit tabs screen
                    if (!tabNavController.canGoBack && tabNavController.current != Tab1) {
                        tabNavController.replace(Tab1)
                    } else {
                        tabNavController.back()
                    }
                }
            }
            // we can go back if there is smth in backstack or if it is not first tab
            val canGoBack by remember {
                derivedStateOf { // prevent extra recompositions
                    tabNavController.canGoBack || tabNavController.current != Tab1
                }
            }
            // custom back handler for system `back` event
            NavBackHandler(canGoBack, backAction)
            // display tabs content
            Navigation(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                navController = tabNavController,
                handleSystemBackEvent = false
            )
            // display tabs
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

val TabScreen1 by navDestination<Unit> { TabScreen("Tab screen 1", TabScreen2) }
val TabScreen2 by navDestination<Unit> { TabScreen("Tab screen 2", TabScreen3) }
val TabScreen3 by navDestination<Unit> { TabScreen("Tab screen 3", null) }