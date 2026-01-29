# Migration Guide: Version 1.5 to 2.0

## Overview
To better separate navigation state management from Compose components e.g., state objects, several classes have been reorganized into dedicated packages.

## Package Reorganization

Classes and functions split onto 2 main packages:
- Core nav items -> `com.composegears.tiamat.navigation`
- Compose elements -> `com.composegears.tiamat.compose`

Removed:
- `tiamat-koin` - use DI's official compose libraries
- `TiamatViewModel` - use official `androidx.lifecycle.ViewModel` instead
- `com.composegears.tiamat.NavBackHandler` - use `androidx.compose.ui.backhandler.BackHandler` instead
- `com.composegears.tiamat.navigation.TiamatViewModel` - use `androidx.lifecycle.ViewModel` instead

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
   The `getNavArgs()`, `getFreeArgs()`, and `getNavResult()` functions now provide actual values rather than `state` objects. Manual updates are required when needed.
   Important: Calling `clearNavResult()` will not trigger recomposition.

   The `back` function no longer provides `orElse` parameter, `recursive` option was added instead to navigate back recursively. New option is safer.


### 4. Serialization
   `NavArgs`, `FreeArgs` and `NavResult` are serializable when implements `NavData` interface
   ```kotlin
   @Serializable
   data class ArchSerializableDataClass(val t: Int) : NavData

   @OptIn(InternalSerializationApi::class)
   private val ArchSerializableDataScreen by navDestination<ArchSerializableDataClass> {
       val navArgs = navArgs()
       // when used with freeArgs & navResult - you need to pass it into generic in order to deserialize
       val freeArgs1 = freeArgs<Any>() // null, not yet deserialized
       val freeArgs2 = freeArgs<Int>() // null, not an Int
       val freeArgs3 = freeArgs<ArchSerializableDataClass>() // data, deserialized
       val freeArgs4 = freeArgs<Any>() // data / freeArgs4 == freeArgs3 as it was deserialized and cached
       /*..*/
   }
   ```

### 5. Animation Scope Changes
   With the introduction of the `scene API`, content is not guaranteed to be within an animated scope:
   `NavDestinationScope` no longer inherits from `AnimatedVisibilityScope`
   Use `LocalNavAnimatedVisibilityScope` to access the current `AnimatedVisibilityScope` when destinations are displayed within the `Navigation(...)` composable

### 6. Observable Properties Migration
   Several observable properties have migrated from `State` to `Flow`. New composable functions are available for manual observation:

```kotlin
@Composable
fun NavController.navStateAsState(): State<NavController.NavState>

@Composable
fun NavController.navStackAsState(): State<List<NavEntry<*>>> 

@Composable
fun NavController.canNavigateBackAsState(): State<Boolean> 

@Composable
fun NavController.currentNavEntryAsState(): State<NavEntry<*>?> 

@Composable
fun NavController.currentNavDestinationAsState(): State<NavDestination<*>?> 

```

### 7. Migration from custom ViewModel to `androidx.lifecycle.ViewModel`

We decided to support official ViewModel's instead of custom solution

> [!IMPORTANT]
> Limitations: ViewModels + `SavedStateHandle` is only supported since 2.2.0 version

### 8. Editing back stack

The `editBackStack` function has been removed. Use `editNavStack` instead:
```kotlin
navController.editNavStack { old ->
    // modify old list and provide a new one
    // the last destination will be displayed (became the current one)
    listOf(/*...*/)
}
```
