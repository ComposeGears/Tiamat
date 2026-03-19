---
name: Tiamat Extra Navigation Actions
description: Use replace/popToTop/back variants, per-call transitions, route-based navigation, stack editing, and NavController state observation.
---

# Goal

Use this skill when an agent needs navigation behaviour beyond basic `navigate` and `back`.

# Core navigation actions

```kotlin
import com.composegears.tiamat.compose.back
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.compose.navigate
import com.composegears.tiamat.compose.replace

val CheckoutScreen by navDestination {
    val nc = navController()

    nc.navigate(OrderSuccessScreen)          // forward, adds current to back stack
    nc.replace(OrderSuccessScreen)           // forward, current NOT added to back stack
    nc.back()                                // go back one step
    nc.back(to = OrdersListScreen)           // back to a specific destination
    nc.back(to = OrdersListScreen, inclusive = true)  // back, also removes the target
    nc.back(recursive = false)              // do NOT bubble to parent NavController
}
```

# Recipe: `popToTop` — tab-bar navigation

Bring an existing destination to the top of the stack without duplicating it, or navigate to it fresh if absent. This is the correct primitive for bottom-tab / side-nav switching.

```kotlin
import com.composegears.tiamat.compose.popToTop

// Selecting a tab: pops back to it if already open, otherwise navigates fresh
nc.popToTop(Tab1Screen)
nc.popToTop(Tab2Screen)

// Custom fallback (instead of the default fresh navigate)
nc.popToTop(Tab3Screen) {
    navigate(Tab3Screen, freeArgs = "deepLink")
}
```

# Recipe: Per-call transition override

Pass a `ContentTransform` to any navigation call to override the global animation for that single transition:

```kotlin
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import com.composegears.tiamat.compose.back
import com.composegears.tiamat.compose.navigate
import com.composegears.tiamat.compose.navigationSlideInFromBottom
import com.composegears.tiamat.compose.navigationSlideOutToBottom

nc.navigate(ModalScreen, transition = scaleIn() togetherWith scaleOut())
nc.navigate(SheetScreen, transition = navigationSlideInFromBottom())
nc.back(transition = navigationSlideOutToBottom())
```

Built-in helpers: `navigationFadeInOut()`, `navigationSlideInOut(isForward)`,
`navigationSlideInFromBottom()`, `navigationSlideOutToBottom()`, `navigationNone()`,
`navigationPlatformDefault(isForward)`.

# Recipe: Route API — build a multi-step back stack in one call

```kotlin
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.navigation.NavController

@OptIn(TiamatExperimentalApi::class)
fun openWizardAtStep3(nc: NavController) {
    nc.route {
        element(WizardStep1)
        element(WizardStep2)
        element(WizardStep3)    // user lands here; Step1 and Step2 are in the back stack
    }
}
```

Navigate by destination name (useful for deep links or cross-module routing where a compile-time reference is unavailable):

```kotlin
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.navigation.NavController

@OptIn(TiamatExperimentalApi::class)
fun openByDeepLink(nc: NavController) {
    nc.route {
        destination("HomeScreen")
        destination("OrderListScreen")
        destination("OrderDetailScreen")
    }
}
```

# Recipe: `editNavStack` — arbitrary stack manipulation

```kotlin
import com.composegears.tiamat.compose.editNavStack
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry

// Clear history — keep only the current screen
nc.editNavStack { old -> old.takeLast(1) }

// Insert a screen just before the current one
nc.editNavStack { old ->
    val current = old.last()
    old.dropLast(1) + SomeScreen.toNavEntry() + current
}

// Apply a backward transition type when removing the last entry
nc.editNavStack(transitionType = NavController.TransitionType.Backward) { old ->
    old.dropLast(1)
}
```

# Recipe: Observe NavController state

Drive UI (back-button visibility, tab highlights, breadcrumbs) from reactive NavController state:

```kotlin
import com.composegears.tiamat.compose.canNavigateBackAsState
import com.composegears.tiamat.compose.currentNavDestinationAsState
import com.composegears.tiamat.compose.currentNavEntryAsState
import com.composegears.tiamat.compose.navStackAsState

val stack        by nc.navStackAsState()                  // full back stack
val currentEntry by nc.currentNavEntryAsState()           // current NavEntry or null
val activeTab    by nc.currentNavDestinationAsState()     // current NavDestination or null
val canGoBack    by nc.canNavigateBackAsState()           // true when stack size > 1
```

# Recipe: Navigate across nested NavControllers

`findParentNavController(key)` walks up the controller hierarchy until it finds one with the given `key`. Use it to escape a nested flow and trigger navigation in a parent controller.

```kotlin
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navigate

val nc = navController()

// Climb to the named root controller and navigate there
nc.findParentNavController("root-nav")?.navigate(MainMenuScreen)
```

# Observe navigation transitions (analytics / logging)

```kotlin
nc.setOnNavigationListener { from, to, type ->
    analytics.track(
        "navigation",
        mapOf("from" to from?.destination?.name, "to" to to?.destination?.name)
    )
}
```

# Notes For Agents

- `nc.back()` returns `false` when the stack cannot go further back — check the return value before performing a fallback.
- `back(recursive = true)` (default) bubbles the back event to the parent `NavController` when the current stack is exhausted. Pass `recursive = false` to prevent bubbling.
- `back(inclusive = true)` removes the target destination itself in addition to all entries above it.
- For tab-based navigation always prefer `popToTop` over `navigate` to avoid duplicate tab entries accumulating in the stack.
- `route { destination("ByName") }` requires the named destination to be registered in the `destinations` array (or graph) of the `Navigation` host — the compiler cannot verify this.
- For overlays (dialogs / bottom sheets in the nav stack) and two-pane layouts, see `tiamat-06-custom-layouts`.
- For ViewModel and retained state, see `tiamat-05-viewmodel-and-state`.
- For destination-level cross-cutting behaviour (analytics, auth guards), see `tiamat-07-extensions`.
