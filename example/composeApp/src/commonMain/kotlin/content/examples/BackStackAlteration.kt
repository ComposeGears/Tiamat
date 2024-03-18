package content.examples

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import content.examples.common.*

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
            BackButton(onClick = navController::back)
        }
    }
}

val BackStackAlterationRoot by navDestination<Unit> {
    SimpleScreen("Nested navigation") {
        Column(Modifier.padding(16.dp)) {
            val nestedNavController = rememberNavController(
                key = "BackStackAlterationNavController",
                startDestination = Screen5,
                destinations = arrayOf(
                    Screen1,
                    Screen2,
                    Screen3,
                    Screen4,
                    Screen5,
                    ScreenAt0,
                    ScreenLast,
                )
            )
            var backstackKey by remember { mutableStateOf(0) }
            TextCaption("Actions:")
            Spacer(16.dp)
            // actions
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton("Remove first") {
                    if (nestedNavController.getBackStack().isNotEmpty())
                        nestedNavController.editBackStack {
                            removeAt(0)
                        }
                    backstackKey++
                }
                TextButton("Add at index 0") {
                    nestedNavController.editBackStack {
                        if (nestedNavController.getBackStack().isNotEmpty()) {
                            add(0, ScreenAt0)
                        } else add(ScreenAt0)
                    }
                    backstackKey++
                }
                TextButton("Add last") {
                    nestedNavController.editBackStack {
                        add(ScreenLast)
                    }
                    backstackKey++
                }
                TextButton("Make 1-2-3-4") {
                    nestedNavController.editBackStack {
                        clear()
                        add(Screen1)
                        add(Screen2)
                        add(Screen3)
                        add(Screen4)
                    }
                    backstackKey++
                }
            }
            Spacer(16.dp)
            // back stack
            TextCaption("Backstack")
            key(backstackKey, nestedNavController.current) {
                LazyRow(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(nestedNavController.getBackStack()) {
                        TextCaption(it.name)
                    }
                    if (nestedNavController.getBackStack().isEmpty()) item {
                        TextCaption("Empty")
                    }
                }
            }
            Spacer(16.dp)
            // navigation
            Box(
                Modifier
                    .border(4.dp, MaterialTheme.colorScheme.onSurface)
                    .padding(4.dp)
            ) {
                Navigation(nestedNavController)
            }
        }
    }
}

val Screen1 by navDestination<Unit> { Screen("Screen 1") }
val Screen2 by navDestination<Unit> { Screen("Screen 2") }
val Screen3 by navDestination<Unit> { Screen("Screen 3") }
val Screen4 by navDestination<Unit> { Screen("Screen 4") }
val Screen5 by navDestination<Unit> { Screen("Screen 5") }
val ScreenAt0 by navDestination<Unit> { Screen("Screen At 0") }
val ScreenLast by navDestination<Unit> { Screen("Screen Last") }