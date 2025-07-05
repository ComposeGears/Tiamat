# Migration Guide: Version 1.5 to 2.0

## Overview
To better separate navigation state management from Compose components e.g., state objects, several classes have been reorganized into dedicated packages.

## Package Reorganization
Moved to `com.composegears.tiamat.navigation` package:
- `TiamatViewModel` class
- `SavedState` class
- `NavController` class
- Other navigation-related classes & functions

Moved to `com.composegears.tiamat.compose` package:
- `rememberNavController(...)` function
- `Navigation(...)` function
- `navController(...)` function
- Other Compose-related classes and functions

Removed:
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
   The `navArgs`, `freeArgs`, and `navResult` functions now provide actual values rather than `state` objects. Manual updates are required when needed.
   Important: Calling `clearNavResult()` will not trigger recomposition if you have an existing `val nr = navResult()` call.

   The `back` function no longer provides `orElse` parameter, `recurceive` option where added instead to navigate back recursively. New option is safer.  

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

### 6. Migration from custom ViewModel to `androidx.lifecycle.ViewModel`

We decide to support official ViewModel's instead of custom solution

> [!IMPORTANT]
> Limitations: ViewModels + `SavedStateHandle` is !NOT! supported due to overengineered solution from Google:

In order to support `SavedStateHandle` they did:
- [Create custom `SavedStateRegistry` and save it via `rememberSaveable`](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:navigation3/navigation3-runtime/src/commonMain/kotlin/androidx/navigation3/runtime/SavedStateNavEntryDecorator.kt
  )
```kotlin
internal class EntrySavedStateRegistry : SavedStateRegistryOwner {/*...*/}
```
- [Create wired object that merge/use multiple unobvious interfaces](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:lifecycle/lifecycle-viewmodel-navigation3/src/commonMain/kotlin/androidx/lifecycle/viewmodel/navigation3/ViewModelStoreNavEntryDecorator.kt;l=91;drc=fb6fafab43a0720b8456e164bda2748b0d29bd56;bpv=0;bpt=1)
```kotlin
object :
     ViewModelStoreOwner,
     SavedStateRegistryOwner by savedStateRegistryOwner,
     HasDefaultViewModelProviderFactory {/*...*/}
```

- Use it as `ViewModelStoreOwner` overriding original one
- All above bound to `Lifecycle` (why? there is `rememberSaveable` under the hood...)

As from our side all the steps above creates quite big level of complexity

In order to save ViewModel's state use:

```kotlin
// ViewModel
private class ArchViewModelSaveableViewModel(
    savedState: MutableSavedState
) : ViewModel() {

    private var _counter = savedState.recordOf("counter", 0)
    val counter = _counter.asStateFlow()

    /*...*/
}

// Compose
val viewModelSavedState = rememberSaveable { MutableSavedState() }
val saveableViewModel = viewModel { ArchViewModelSaveableViewModel(viewModelSavedState) }
```
> [!IMPORTANT]
> Solution/syntax may be changed depend on feedbacks