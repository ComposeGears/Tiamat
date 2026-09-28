# Content Extensions

Wrap destination UI with `ContentExtension`.

## How it works

`ContentExtension` receives the destination's primary content as a lambda parameter named `entryContent`. The extension must call it exactly once to render the destination. This makes the extension a wrapper around the screen instead of a replacement.

```kotlin
import androidx.compose.runtime.Composable
import com.composegears.tiamat.compose.ContentExtension
import com.composegears.tiamat.compose.NavDestinationScope
import com.composegears.tiamat.compose.navDestination

class AnalyticsExtension(val screenName: String) : ContentExtension<Any> {
    @Composable
    override fun NavDestinationScope<out Any>.Content(
        entryContent: @Composable () -> Unit,
    ) {
        LaunchedEffect(screenName) {
            analytics.trackScreen(screenName)
        }
        entryContent()
    }
}

val HomeScreen by navDestination(AnalyticsExtension("home")) { /* ... */ }
val ProfileScreen by navDestination(AnalyticsExtension("profile")) { /* ... */ }
```

### Wrap content with `extension {}`

```kotlin
import com.composegears.tiamat.compose.extension

val debugBannerExt = extension<Any> { entryContent ->
    Box(Modifier.fillMaxSize()) {
        entryContent()
        Text("DEBUG", Modifier.align(Alignment.TopEnd).padding(8.dp))
    }
}

val HomeScreen by navDestination(debugBannerExt) { /* ... */ }
```

### Notes

- `extension {}` creates an anonymous `ContentExtensionImpl` — it cannot be retrieved by type via `ext<T>()`. Use a named class for that.
- Call `entryContent()` exactly once, typically before or after your own UI code depending on whether you want to decorate the screen or wrap it.
- Multiple content extensions are nested in declaration order: the last declared extension is the outermost wrapper and runs first.
- Use `NavExtension` for marker/data-only destinations; use `ContentExtension` when you need a composable wrapper around the destination content.
- Extensions declared as `object` (singletons) allow callers to query live state via properties, making them suitable for cross-screen coordination.
