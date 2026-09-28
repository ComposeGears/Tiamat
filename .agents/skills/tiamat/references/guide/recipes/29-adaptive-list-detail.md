# Adaptive List-Detail

Responsive layout that switches between single-pane and two-pane based on window size class.

## How it works

Use `BoxWithConstraints` with `WindowSizeClass` to decide the layout, and `NavigationScene` with `movableContentOf` to move the navigation content between layouts without losing state. On compact screens, show a single `AnimatedContent` pane. On wider screens, show the list fixed on the left and the detail on the right.

[![](https://img.shields.io/badge/Full_sample-GitHub-blue)](https://github.com/nicehash/Tiamat/blob/main/sample/shared/src/commonMain/kotlin/composegears/tiamat/sample/content/layouts/LayoutAdaptiveListDetails.kt)

```kotlin
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
val AdaptiveListDetailScreen by navDestination {
    BoxWithConstraints {
        val sizeClass by remember(maxWidth) {
            mutableStateOf(
                WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight)).widthSizeClass
            )
        }
        val nc = rememberNavController(
            key = "adaptive-nav",
            startDestination = ListScreen
        )
        val content = remember {
            movableContentOf { sizeClass: WindowWidthSizeClass ->
                NavigationScene(
                    navController = nc,
                    destinations = arrayOf(ListScreen, Detail1Screen, Detail2Screen),
                ) {
                    val current by nc.currentNavEntryAsState()
                    if (sizeClass == WindowWidthSizeClass.Compact) {
                        // Single pane
                        AnimatedContent(
                            targetState = current,
                            contentKey = { it?.contentKey() },
                            transitionSpec = { navigationFadeInOut() },
                            modifier = Modifier.fillMaxSize()
                        ) { EntryContent(it) }
                    } else {
                        // Two pane
                        Row(Modifier.fillMaxSize()) {
                            val navStack by nc.navStackAsState()
                            val listEntry by remember(navStack) {
                                derivedStateOf { navStack.firstOrNull() }
                            }
                            val itemEntry by remember(navStack) {
                                derivedStateOf { navStack.lastOrNull().takeIf { it != listEntry } }
                            }
                            Box(Modifier.width(300.dp).fillMaxHeight()) {
                                EntryContent(listEntry)
                            }
                            AnimatedContent(
                                targetState = itemEntry,
                                contentKey = { it?.contentKey() },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                transitionSpec = { navigationFadeInOut() }
                            ) {
                                if (it != null) EntryContent(it)
                                else Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No item selected")
                                }
                            }
                        }
                    }
                }
            }
        }
        content(sizeClass)
    }
}
```

### Key points

- `movableContentOf` ensures the navigation scene is moved between layouts without recreation.
- On compact screens, use `NavigationBar`; on wider screens, use `NavigationRail`.
- The list pane is always the first stack entry; the detail pane is the last entry (if different from the list).

