---
name: Tiamat Destination Basics
description: Create portable Compose screens with `navDestination`, wire them into a `Navigation` host, navigate between them, and preview destinations in isolation.
---

# Goal

Use this skill when an agent needs to add a new Tiamat screen in any Kotlin Multiplatform or Compose project.

# Prerequisites

- Tiamat is added to the project dependencies or version catalog.
- A Compose module already exists.

```kotlin
// build.gradle.kts (module)
dependencies {
    implementation("io.github.composegears:tiamat:<version>")
}
```

# Recipe: Simple screen with `navDestination`

```kotlin
import com.composegears.tiamat.compose.navDestination

val HomeScreen by navDestination {
    // compose content of the screen
}
```

# Recipe: Add destination to a navigation host

```kotlin
import androidx.compose.runtime.Composable
import com.composegears.tiamat.compose.Navigation
import com.composegears.tiamat.compose.rememberNavController

@Composable
fun AppNavigation() {
    val nc = rememberNavController(
        startDestination = HomeScreen,
    )

    Navigation(
        navController = nc,
        destinations = arrayOf(
            HomeScreen,
            DetailsScreen,
        )
    )
}
```

# Recipe: Navigate between screens

```kotlin
import com.composegears.tiamat.compose.back
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.compose.navigate

val DetailsScreen by navDestination {
    val nc = navController()

    Button(onClick = { nc.back() }) {
        Text("Back")
    }
}

val HomeScreen by navDestination {
    val nc = navController()

    Button(onClick = { nc.navigate(DetailsScreen) }) {
        Text("Open details")
    }
}
```

# Recipe: Preview a destination with `TiamatPreview`

Use `TiamatPreview` to render any destination inside a Compose `@Preview` without a real navigation stack.

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.composegears.tiamat.compose.TiamatPreview

@Preview
@Composable
private fun HomeScreenPreview() {
    TiamatPreview(destination = HomeScreen)
}
```

For destinations that require typed args:

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.composegears.tiamat.compose.TiamatPreview

data class ItemArgs(val id: Int, val title: String)

val ItemScreen by navDestination<ItemArgs> { /* ... */ }

@Preview
@Composable
private fun ItemScreenPreview() {
    TiamatPreview(
        destination = ItemScreen,
        navArgs = ItemArgs(id = 1, title = "Preview item"),
    )
}
```

`TiamatPreview` also accepts `freeArgs` and `navResult` to test those states in isolation.

# Recipe: Set a global transition style

Override the default fade animation for the entire `Navigation` host via `contentTransformProvider`:

```kotlin
import com.composegears.tiamat.compose.Navigation
import com.composegears.tiamat.compose.navigationSlideInOut

Navigation(
    navController = nc,
    destinations = arrayOf(HomeScreen, DetailsScreen),
    contentTransformProvider = { isForward -> navigationSlideInOut(isForward) }
)
```

Built-in helpers: `navigationFadeInOut()` (default), `navigationSlideInOut(isForward)`,
`navigationSlideInFromBottom()`, `navigationSlideOutToBottom()`, `navigationNone()`,
`navigationPlatformDefault(isForward)`.

# Notes For Agents

- Keep destination names domain-based (`ProfileScreen`, `OrderListScreen`) instead of UI-based (`Screen1`).
- If Kotlin reports recursive type inference for chained destinations, add an explicit type:

```kotlin
import com.composegears.tiamat.navigation.NavDestination

val ProfileScreen: NavDestination<Unit> by navDestination { /* ... */ }
```

- Register every reachable destination in `Navigation(destinations = arrayOf(...))` unless the project uses generated graph mode (see `tiamat-04-graph-usage`).
- Pass `saveable = false` to `rememberNavController` for ephemeral sub-flows that must not survive process restart:

```kotlin
val nc = rememberNavController(
    startDestination = HomeScreen,
    saveable = false,
)
```

- Pass `handleSystemBackEvent = false` to `Navigation` when the containing layout manages back navigation itself (e.g., a detail pane in a two-pane layout):

```kotlin
Navigation(
    navController = nc,
    destinations = arrayOf(HomeScreen, DetailsScreen),
    handleSystemBackEvent = false,
)
```
