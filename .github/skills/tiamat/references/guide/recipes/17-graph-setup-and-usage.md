# Graph Setup and Usage

Configure `TiamatGraph`, `@InstallIn`, install in multiple graphs, and merge graphs.

## Prerequisites

Add both the destinations compiler plugin and destinations library:

```kotlin
// build.gradle.kts (module)
plugins {
    id("io.github.composegears.tiamat.destinations.compiler") version "$version"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.composegears:tiamat-destinations:$version")
        }
    }
}
```

## Create a graph and install destinations

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

## Use the graph in `Navigation`

```kotlin
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

## Install in multiple graphs

`@InstallIn` is `@Repeatable`:

```kotlin
@OptIn(TiamatExperimentalApi::class)
@InstallIn(MainGraph::class)
@InstallIn(OnboardingGraph::class)
val SharedConfirmationScreen by navDestination { /* ... */ }
```

## Merge graphs from different modules

```kotlin
@OptIn(TiamatExperimentalApi::class)
object FeatureAGraph : TiamatGraph

@OptIn(TiamatExperimentalApi::class)
object FeatureBGraph : TiamatGraph

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

## Dump graph report

Set the `tiamat.destinations.dumpDir` Gradle property to generate a `report.md` file listing all graphs and their destinations at compile time:

```properties
# gradle.properties
tiamat.destinations.dumpDir=build/tiamat-graph
```

The generated `report.md` uses the following format:

```markdown
## MainGraph
- OrdersListScreen
- OrderDetailsScreen

## OnboardingGraph
- WelcomeScreen
- SharedConfirmationScreen
```

### Notes

- Use graph mode for large projects to avoid manually maintaining `destinations = arrayOf(...)`.
- A destination not appearing at runtime almost always means a missing `@InstallIn` annotation or the compiler plugin is absent.
- `@InstallIn` targets both `PROPERTY` and `CLASS` declarations.

