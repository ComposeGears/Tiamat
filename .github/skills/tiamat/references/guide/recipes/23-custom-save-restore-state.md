# Custom Save and Restore State

Manually save and restore a NavController's state with `saveToSavedState()` and `rememberNavController(savedState = ...)`.

## How it works

Call `nc.saveToSavedState()` to snapshot the entire navigation stack into a `SavedState` object. Later, pass that snapshot to `rememberNavController(savedState = ...)` to restore the exact same stack.

This is useful when you need to tear down and recreate a navigation flow while preserving its state (e.g., toggling between a navigation view and a debug/inspector view).

```kotlin
import androidx.compose.runtime.*
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.SavedState

val CustomSaveStateScreen by navDestination {
    var savedState by remember { mutableStateOf<SavedState?>(null) }
    var showNavigation by remember { mutableStateOf(true) }

    Column {
        Button(onClick = { showNavigation = !showNavigation }) {
            Text(if (showNavigation) "Save & hide" else "Restore")
        }

        if (showNavigation) {
            val nc = rememberNavController(
                key = "my-flow",
                startDestination = Step1Screen,
                savedState = savedState,
            )
            Navigation(
                navController = nc,
                destinations = arrayOf(Step1Screen, Step2Screen, Step3Screen),
            )
            DisposableEffect(Unit) {
                onDispose {
                    savedState = nc.saveToSavedState()
                }
            }
        } else {
            Text("Navigation state saved. Tap Restore to bring it back.")
        }
    }
}
```

### Key points

- `saveToSavedState()` captures the full stack including args marked as `NavData`.
- The `savedState` parameter takes priority over `startDestination` when restoring.
- Use `DisposableEffect` + `onDispose` to save state right before the NavController leaves composition.

