# Tab Navigation with Multiple Back Stacks

Implement tab navigation where each tab has its own independent back stack.

## How it works

Create a parent `NavController` that manages top-level tabs with `popToTop`. Inside each tab, create a separate `NavController` (with a unique `key`) that manages that tab's own navigation stack. State is retained for each tab when switching between them.

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavDestination

val TabsScreen by navDestination {
    Column(Modifier.fillMaxSize()) {
        val tabs = arrayOf<NavDestination<*>>(Tab1, Tab2, Tab3)
        val nc = rememberNavController(
            key = "tabs-nav",
            startDestination = Tab1,
        )
        val activeTab by nc.currentNavDestinationAsState()

        // Tab content area
        Navigation(
            navController = nc,
            destinations = tabs,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )

        // Tab bar
        Row(Modifier.fillMaxWidth()) {
            tabs.forEach { tab ->
                Button(
                    onClick = { nc.popToTop(tab) },
                    enabled = activeTab != tab,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(tab.name)
                }
            }
        }
    }
}
```

Each tab destination contains its own nested `NavController` for in-tab navigation:

```kotlin
val Tab1 by navDestination {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Tab 1")
        val nc = rememberNavController(
            key = "tab1-content",
            startDestination = Tab1Screen1,
        )
        Navigation(
            navController = nc,
            destinations = arrayOf(Tab1Screen1, Tab1Screen2),
        )
    }
}
```

### Key points

- Use `popToTop` (not `navigate`) to switch tabs — it avoids duplicating tab entries in the stack.
- Give each tab's inner `NavController` a unique `key` so their stacks are independent.
- `currentNavDestinationAsState()` provides the active tab for highlighting.

