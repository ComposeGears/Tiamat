# Content Extensions

Inject composable UI overlays/underlays with `ContentExtension`.

## How it works

Implement `ContentExtension` to inject a `@Composable` layer on top of (Overlay, default) or beneath (Underlay) the destination's UI.

```kotlin
import androidx.compose.runtime.Composable
import com.composegears.tiamat.compose.ContentExtension
import com.composegears.tiamat.compose.NavDestinationScope
import com.composegears.tiamat.compose.navDestination

class AnalyticsExtension(val screenName: String) : ContentExtension<Any> {
    @Composable
    override fun NavDestinationScope<out Any>.Content() {
        LaunchedEffect(Unit) {
            analytics.trackScreen(screenName)
        }
    }
    // Default type is Overlay. Override to render *behind* the destination:
    // override fun getType() = ContentExtension.Type.Underlay
}

val HomeScreen by navDestination(AnalyticsExtension("home")) { /* ... */ }
val ProfileScreen by navDestination(AnalyticsExtension("profile")) { /* ... */ }
```

### Quick one-off overlay with `extension {}`

```kotlin
import com.composegears.tiamat.compose.extension

val debugBannerExt = extension<Any> {
    Box(Modifier.fillMaxSize()) {
        Text("DEBUG", Modifier.align(Alignment.TopEnd).padding(8.dp))
    }
}

val HomeScreen by navDestination(debugBannerExt) { /* ... */ }
```

### Notes

- `extension {}` creates an anonymous `ContentExtensionImpl` — it cannot be retrieved by type via `ext<T>()`. Use a named class for that.
- `ContentExtension.Type.Overlay` (default) renders on top; `Underlay` renders behind.
- Extensions declared as `object` (singletons) allow callers to query live state via properties, making them suitable for cross-screen coordination.

