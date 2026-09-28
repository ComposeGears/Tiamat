# Marker Extensions

Attach metadata to destinations with `NavExtension`.

## How it works

Implement `NavExtension` with no `@Composable` content to attach pure metadata. Query it with `ext<T>()`.

### Simple marker (tag)

```kotlin
import com.composegears.tiamat.compose.NavExtension
import com.composegears.tiamat.compose.ext
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.compose.replace

class RequiresAuthExtension : NavExtension<Any?>

val DashboardScreen by navDestination(RequiresAuthExtension()) { /* ... */ }

// Query from a navigation listener
nc.setOnNavigationListener { _, to, _ ->
    val needsAuth = to?.destination?.ext<RequiresAuthExtension>() != null
    if (needsAuth && !authManager.isLoggedIn()) {
        nc.replace(LoginScreen)
    }
}
```

### Data-carrying marker

```kotlin
class ScreenMetaExtension(val title: String, val trackingId: String) : NavExtension<Any?>

val OrderListScreen by navDestination(ScreenMetaExtension("Orders", "order_list")) { /* ... */ }

val meta = nc.getCurrentNavEntry()?.destination?.ext<ScreenMetaExtension>()
topBarTitle = meta?.title ?: ""
```

### Read from inside the destination

```kotlin
val AnalyticsScreen by navDestination(AnalyticsExtension("detail")) {
    val meta = ext<AnalyticsExtension>()
    Text("Tracking as: ${meta?.screenName}")
}
```

### Multiple extensions on one destination

```kotlin
val CheckoutScreen by navDestination(
    RequiresAuthExtension(),
    AnalyticsExtension("checkout"),
    debugBannerExt,
) { /* ... */ }
```

### Notes

- Extensions are declared as `vararg` in `navDestination(ext1, ext2, ...)`.
- Extensions are stored on the `ComposeNavDestination` object and shared across all instances. Do not hold per-instance mutable state unless the extension is a singleton `object`.

