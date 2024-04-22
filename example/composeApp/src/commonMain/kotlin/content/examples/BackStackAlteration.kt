package content.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    SimpleScreen("Back stack alteration") {
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
            var backstackKey by remember { mutableIntStateOf(0) }
            TextCaption("Actions:")
            Spacer(4.dp)
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
            TextCaption("Backstack")
            key(backstackKey, nestedNavController.currentNavEntry) {
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(nestedNavController.getBackStack()) {
                        Chip(it.destination.name)
                    }
                    if (nestedNavController.getBackStack().isEmpty()) item {
                        Chip("Empty")
                    }
                }
            }
            Spacer(16.dp)
            Box(
                modifier = Modifier
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

@Composable
private fun Chip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .defaultMinSize(minWidth = 64.dp)
            .background(MaterialTheme.colorScheme.onSurface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.surface,
            style = MaterialTheme.typography.labelMedium
        )
    }
}