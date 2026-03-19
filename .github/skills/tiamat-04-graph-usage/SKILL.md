---
name: Tiamat Generated Graph Usage
description: Configure and use `TiamatGraph` with `@InstallIn` for generated destination registration, install destinations in multiple graphs, and merge graphs from different modules.
---

# Goal

Use this skill when an agent needs graph-based destination registration instead of manually listing destinations.

# Required setup

Before using `TiamatGraph` and `@InstallIn`, add both the destinations compiler plugin and destinations library using your project's references.

```kotlin
// build.gradle.kts (module)
plugins {
    // Replace with your plugin reference (version-catalog alias or id)
    id("io.github.composegears.tiamat.destinations.compiler") version "$version"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Replace with your dependency reference (catalog alias or coordinate)
            implementation("io.github.composegears:tiamat-destinations:$version")
        }
    }
}
```

# Recipe: Create a graph and install destinations

```kotlin
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.destinations.InstallIn
import com.composegears.tiamat.destinations.TiamatGraph

@OptIn(TiamatExperimentalApi::class)
private object MainGraph : TiamatGraph

@OptIn(TiamatExperimentalApi::class)
@InstallIn(MainGraph::class)
val OrdersListScreen by navDestination { /* ... */ }

@OptIn(TiamatExperimentalApi::class)
@InstallIn(MainGraph::class)
val OrderDetailsScreen by navDestination<Int> { /* ... */ }
```

# Recipe: Use the graph in `Navigation`

```kotlin
import androidx.compose.runtime.Composable
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.compose.Navigation
import com.composegears.tiamat.compose.rememberNavController

@Composable
@OptIn(TiamatExperimentalApi::class)
fun AppNavigation() {
    val nc = rememberNavController(
        key = "main-nav",
        startDestination = OrdersListScreen,
    )

    Navigation(
        navController = nc,
        graph = MainGraph,
    )
}
```

# Recipe: Install a destination in multiple graphs

`@InstallIn` is `@Repeatable` — apply it more than once to share a destination across several graphs:

```kotlin
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.destinations.InstallIn

@OptIn(TiamatExperimentalApi::class)
@InstallIn(MainGraph::class)
@InstallIn(OnboardingGraph::class)
val SharedConfirmationScreen by navDestination { /* ... */ }
```

# Recipe: Merge graphs from different modules

Use the `+` operator on `TiamatGraph` to combine two graphs. The merged result contains unique destinations from both:

```kotlin
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.destinations.TiamatGraph

// Each feature module declares its own graph
@OptIn(TiamatExperimentalApi::class)
object FeatureAGraph : TiamatGraph

@OptIn(TiamatExperimentalApi::class)
object FeatureBGraph : TiamatGraph

// App module merges them
@Composable
@OptIn(TiamatExperimentalApi::class)
fun AppNavigation() {
    val nc = rememberNavController(startDestination = FeatureAStartScreen)
    Navigation(
        navController = nc,
        graph = FeatureAGraph + FeatureBGraph,
    )
}
```

# Notes For Agents

- Use graph mode for large projects to avoid manually maintaining `destinations = arrayOf(...)`.
- Keep all destinations that belong together in the same `TiamatGraph` object.
- A destination not appearing at runtime almost always means a missing `@InstallIn` annotation or the compiler plugin is absent from the module's `build.gradle.kts`.
- The graph object should be `private` or `internal` to the module that owns the flow; only expose it via the merge API if other modules need to combine it.
- `@InstallIn` targets both `PROPERTY` and `CLASS` declarations — it can be placed on the `val` property or on a class that delegates.
