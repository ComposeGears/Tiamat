# Replace and Pop-to-Top

Use `replace`, `back(to=…)`, and `popToTop` for tab-style and flow navigation.

## How it works

```kotlin
import com.composegears.tiamat.compose.back
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.compose.navigate
import com.composegears.tiamat.compose.popToTop
import com.composegears.tiamat.compose.replace

val CheckoutScreen by navDestination {
    val nc = navController()

    nc.navigate(OrderSuccessScreen)                       // forward, adds current to back stack
    nc.replace(OrderSuccessScreen)                        // forward, current NOT added to back stack
    nc.back()                                             // go back one step
    nc.back(to = OrdersListScreen)                        // back to a specific destination
    nc.back(to = OrdersListScreen, inclusive = true)      // back, also removes the target
    nc.back(recursive = false)                            // do NOT bubble to parent NavController
}
```

### `popToTop` — tab-bar navigation

Bring an existing destination to the top of the stack without duplicating it, or navigate to it fresh if absent:

```kotlin
nc.popToTop(Tab1Screen)
nc.popToTop(Tab2Screen)

// Custom fallback (instead of the default fresh navigate)
nc.popToTop(Tab3Screen) {
    navigate(Tab3Screen, freeArgs = "deepLink")
}
```

### Notes

- `nc.back()` returns `false` when the stack cannot go further back.
- `back(recursive = true)` (default) bubbles to the parent `NavController`. Pass `recursive = false` to prevent bubbling.
- `back(inclusive = true)` removes the target destination itself in addition to all entries above it.
- For tab-based navigation always prefer `popToTop` over `navigate` to avoid duplicate entries.

