# Two-Pane Layout

List-detail / two-pane layout with `NavigationScene`.

## How it works

Use `NavigationScene` to split the stack into a list pane (first entry) and a detail pane (last entry).

```kotlin
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.composegears.tiamat.compose.NavigationScene
import com.composegears.tiamat.compose.navStackAsState
import com.composegears.tiamat.compose.navigationFadeInOut

NavigationScene(
    navController = nc,
    destinations = arrayOf(ListScreen, DetailScreen),
) {
    Row {
        val stack by nc.navStackAsState()
        val listEntry   by remember(stack) { derivedStateOf { stack.firstOrNull() } }
        val detailEntry by remember(stack) { derivedStateOf { stack.lastOrNull()?.takeIf { stack.size > 1 } } }

        Box(Modifier.weight(1f).fillMaxHeight()) {
            EntryContent(listEntry)
        }
        AnimatedContent(
            targetState = detailEntry,
            contentKey = { it?.contentKey() },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            transitionSpec = { navigationFadeInOut() },
        ) {
            EntryContent(it)
        }
    }
}
```

