---
name: Tiamat Extensions
description: Attach cross-cutting behaviour to destinations using `NavExtension` (marker/data) and `ContentExtension` (composable UI overlay or underlay). Query extensions with `ext<T>()`.
---

# Goal

Use this skill when an agent needs behaviour that applies to multiple destinations — analytics, auth guards, UI chrome (banners, debug overlays) — without modifying each destination's body.

# Recipe: Marker extension (metadata, no UI)

Implement `NavExtension` with no `@Composable` content to attach pure metadata. Query it with `ext<T>()` from outside the destination (e.g., in a navigation listener or in the host).

```kotlin
import com.composegears.tiamat.compose.NavExtension
import com.composegears.tiamat.compose.ext
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.compose.replace

// Declaration — acts as a tag, no UI
class RequiresAuthExtension : NavExtension<Any?>

// Attach to a destination
val DashboardScreen by navDestination(RequiresAuthExtension()) { /* ... */ }

// Query from a navigation listener
nc.setOnNavigationListener { _, to, _ ->
    val needsAuth = to?.destination?.ext<RequiresAuthExtension>() != null
    if (needsAuth && !authManager.isLoggedIn()) {
        nc.replace(LoginScreen)
    }
}
```

# Recipe: Data-carrying marker extension

Extensions can carry read-only data accessible anywhere that has the `NavDestination` reference:

```kotlin
import com.composegears.tiamat.compose.NavExtension
import com.composegears.tiamat.compose.ext
import com.composegears.tiamat.compose.navDestination

class ScreenMetaExtension(val title: String, val trackingId: String) : NavExtension<Any?>

val OrderListScreen by navDestination(ScreenMetaExtension("Orders", "order_list")) { /* ... */ }

// Read from outside:
val meta = nc.getCurrentNavEntry()?.destination?.ext<ScreenMetaExtension>()
topBarTitle = meta?.title ?: ""
```

# Recipe: `ContentExtension` — composable UI overlay or underlay

Implement `ContentExtension` to inject a `@Composable` layer on top of (Overlay, default) or beneath (Underlay) the destination's own UI. The extension body has full access to the `NavDestinationScope`.

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

# Recipe: Quick one-off overlay with `extension {}`

Use the `extension {}` helper for a lightweight UI extension when you don't need a named class:

```kotlin
import com.composegears.tiamat.compose.extension
import com.composegears.tiamat.compose.navDestination

val debugBannerExt = extension<Any> {
    Box(Modifier.fillMaxSize()) {
        Text("DEBUG", Modifier.align(Alignment.TopEnd).padding(8.dp))
    }
}

val HomeScreen by navDestination(debugBannerExt) { /* ... */ }
```

> `extension {}` always creates an anonymous `ContentExtensionImpl`. It cannot be retrieved by type via `ext<T>()` — use a named class for that.

# Recipe: Read the extension from inside the destination

```kotlin
import com.composegears.tiamat.compose.ext
import com.composegears.tiamat.compose.navDestination

val AnalyticsScreen by navDestination(AnalyticsExtension("detail")) {
    val meta = ext<AnalyticsExtension>()
    Text("Tracking as: ${meta?.screenName}")
}
```

# Recipe: Multiple extensions on one destination

```kotlin
val CheckoutScreen by navDestination(
    RequiresAuthExtension(),
    AnalyticsExtension("checkout"),
    debugBannerExt,
) { /* ... */ }
```

# Notes For Agents

- Extensions are declared as `vararg` in `navDestination(ext1, ext2, ...)` — any number may be attached.
- `ContentExtension.Type.Overlay` (default) renders on top of the destination content; `Underlay` renders behind it.
- Extensions are stored on the `ComposeNavDestination` object and shared across all instances of that destination. Do **not** hold per-instance mutable state inside an extension object unless it is a singleton (`object`).
- The `extension {}` helper is convenient but its type is always `ContentExtensionImpl`; to retrieve it later with `ext<T>()`, declare a named class instead.
- Extensions declared as `object` (singletons) allow callers to query live state via properties (e.g., `GlobalExtension.activeDestination`) making them suitable for cross-screen coordination.
