# NavigationScene Basics

Full layout control with `NavigationScene`.

## How it works

`NavigationScene` provides a `NavigationSceneScope` with `EntryContent(entry)`. It handles back-handling and destination loading; you supply the animation and layout.

```kotlin
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.getValue
import com.composegears.tiamat.compose.NavigationScene
import com.composegears.tiamat.compose.currentNavEntryAsState
import com.composegears.tiamat.compose.navigationFadeInOut
import com.composegears.tiamat.navigation.NavEntry

NavigationScene(
    navController = nc,
    destinations = arrayOf(HomeScreen, DetailsScreen),
) {
    val currentEntry by nc.currentNavEntryAsState()
    AnimatedContent(
        targetState = currentEntry,
        contentKey = { it?.contentKey() },
        transitionSpec = { navigationFadeInOut() },
    ) { entry: NavEntry<*>? ->
        EntryContent(entry)
    }
}
```

### Notes

- `NavigationScene` does not provide a default animation — you must wrap `EntryContent` in `AnimatedContent` (or equivalent).
- `contentKey = { it?.contentKey() }` is required so Compose treats each `NavEntry` as unique.
- `EntryContent(null)` is a safe no-op.
- Set `handleSystemBackEvent = false` for secondary panes that should not intercept the system back gesture.

