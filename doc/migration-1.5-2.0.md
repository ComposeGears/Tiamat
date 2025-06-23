# Migration Guide: Version 1.5 to 2.0

## Overview
To better separate navigation state management from Compose components e.g., state objects, several classes have been reorganized into dedicated packages.

## Package Reorganization
Moved to `com.composegears.tiamat.navigation` package:
- `TiamatViewModel` class
- `SavedState` class
- `NavController` class
- Other navigation-related classes

Moved to `com.composegears.tiamat.compose` package:
- `rememberNavController(...)` function
- `Navigation(...)` function
- `navController(...)` function
- Other Compose-related classes and functions

Removed:
`com.composegears.tiamat.NavBackHandler` - use `androidx.compose.ui.backhandler.BackHandler` instead

The navController has been decoupled from Compose dependencies, enabling you to test navigation logic without requiring Compose dependencies. Compose-related code is now consolidated in a separate package for improved organization and easier navigation.

## Breaking Changes
### 1. Navigation Controller Configuration
   Before v1.5:
```kotlin
val rootNavController = rememberNavController(
    key = "rootNavController",
    storageMode = StorageMode.Memory, // Fixed storage modes
    startDestination = HomeScreen,
    destinations = arrayOf(/*...*/),  // List of known destinations
    configuration = navControllerConfig
)
Navigation(
    navController = rootNavController,
    modifier = Modifier.fillMaxSize(),
    contentTransformProvider = { navigationPlatformDefault(it) }
)
```
After v2.0:
```kotlin
val rootNavController = rememberNavController(
    key = "rootNavController",
    startDestination = HomeScreen,
    saveable = true,                        // Simple boolean flag
    configuration = navControllerConfig
)
Navigation(
    navController = rootNavController,
    destinations = arrayOf(/*...*/),        // List of known destinations
    modifier = Modifier.fillMaxSize(),
    contentTransformProvider = { navigationPlatformDefault(it) }
)
```
Key Changes:
1) `storageMode` parameter replaced with simplified saveable `boolean` flag
2) `destinations` parameter moved from `rememberNavController` to `Navigation` composable for just-in-time resolution

### 2. Why Destinations Are Still Required
   When saving state, only the destination `name` can be persisted. During rendering, the system needs to convert the `name` back to a `destination` object. The destinations list enables this lookup functionality.

### 3. Navigation Functions Behavior Change
   The `navArgs`, `freeArgs`, and `navResult` functions now provide actual values rather than `state` objects. Manual updates are required when needed.
   Important: Calling `clearNavResult()` will not trigger recomposition if you have an existing `val nr = navResult()` call.

### 4. Animation Scope Changes
   With the introduction of the `scene API`, content is not guaranteed to be within an animated scope:
   `NavDestinationScope` no longer inherits from `AnimatedVisibilityScope`
   Use `LocalNavAnimatedVisibilityScope` to access the current `AnimatedVisibilityScope` when destinations are displayed within the `Navigation(...)` composable

### 5. Observable Properties Migration
   Several observable properties have migrated from `State` to `Flow`. New composable functions are available for manual observation:

```kotlin
@Composable
fun NavController.hasBackEntriesAsState(): State<Boolean> 

@Composable
fun NavController.currentNavEntryAsState(): State<NavEntry<*>?> 

@Composable
fun NavController.currentNavDestinationAsState(): State<NavDestination<*>?>

```