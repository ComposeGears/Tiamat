Tiamat is a Compose Multiplatform navigation library that gives you full control over your navigation stack. It provides:

- **`navDestination`** — a delegate that turns any `@Composable` lambda into a portable, self-contained screen
- **`NavController`** — manages a typed back stack with `navigate`, `back`, `replace`, `popToTop`, `route`, and `editNavStack`
- **`Navigation`** — a composable host that observes the `NavController` and renders the current destination with animated transitions
- **`NavigationScene`** — a lower-level host for full layout control (two-pane, overlays, adaptive layouts)
- **Typed arguments & results** — pass data between screens with `navArgs()`, `freeArgs`, `NavData`, and `navResult`
- **ViewModel & retained state** — screen-scoped and shared ViewModels, `retain {}`, `produceRetainedState`
- **Generated graphs** — `TiamatGraph` + `@InstallIn` compiler plugin for automatic destination registration
- **Extensions** — `NavExtension` (marker/data) and `ContentExtension` (composable overlay/underlay) for cross-cutting concerns

## Getting started

Add Tiamat to your module dependencies:

```kotlin
// build.gradle.kts (module)
dependencies {
    implementation("io.github.composegears:tiamat:<version>")
}
```

Create a destination and a navigation host:

```kotlin
import com.composegears.tiamat.compose.Navigation
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.compose.rememberNavController

val HomeScreen by navDestination {
    Text("Hello, Tiamat!")
}

@Composable
fun App() {
    val nc = rememberNavController(startDestination = HomeScreen)
    Navigation(navController = nc, destinations = arrayOf(HomeScreen))
}
```

## Key concepts

- **Destination** — a `ComposeNavDestination<Args>` created via the `navDestination` delegate. It defines the composable content and optional typed args.
- **NavController** — holds the navigation stack. Created with `rememberNavController`. Supports `saveable` for process-death persistence.
- **Navigation / NavigationScene** — composable hosts. `Navigation` provides default `AnimatedContent` transitions. `NavigationScene` gives you raw `EntryContent(entry)` for custom layouts.
- **NavEntry** — a single entry in the back stack, pairing a destination with its args and state.

## Notes for agents

- Keep destination names domain-based (`ProfileScreen`, `OrderListScreen`) instead of UI-based (`Screen1`).
- Register every reachable destination in `Navigation(destinations = arrayOf(...))` unless the project uses generated graph mode.
- If Kotlin reports recursive type inference for chained destinations, add an explicit type: `val ProfileScreen: NavDestination<Unit> by navDestination { … }`.
- Pass `saveable = false` to `rememberNavController` for ephemeral sub-flows that must not survive process restart.
- Pass `handleSystemBackEvent = false` to `Navigation` when the containing layout manages back navigation itself.

