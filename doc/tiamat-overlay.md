Tiamat Overlay
--------------

`tiamat-overlay` adds a local overlay navigation stack to a Tiamat destination while keeping the host content visible underneath. It is intended for dialogs, bottom sheets, and nested modal flows that should not replace the current screen.

## Setup

Add the module dependency to your target:

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("io.github.composegears:tiamat-overlay:$version")
    }
}
```

The module depends on the core `tiamat` library, so you only need to add `tiamat-overlay` when you want the overlay helpers.

## Usage

Attach an `OverlaysExtension` to the destination that should host a local overlay stack:

```kotlin
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.overlay.OverlaysExtension

val SettingsScreen by navDestination(
    OverlaysExtension(
        destinations = arrayOf(
            EditProfileDialog,
            DeleteAccountSheet,
        )
    )
) {
    val overlays = ext<OverlaysExtension>() ?: error("OverlaysExtension is missing")
    val overlayNavController = overlays.overlayNavController()

    Column {
        Text("Settings")
        Button(onClick = { overlayNavController.navigate(EditProfileDialog) }) {
            Text("Edit profile")
        }
    }
}
```

The extension creates a dedicated overlay `NavController` for the host destination. The host screen stays mounted under the overlay content.

## Overlay destinations

Overlay destinations are normal `navDestination` entries, opened through the local overlay controller:

```kotlin
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.overlay.overlayBack

val EditProfileDialog by navDestination {
    val overlayNavController = navController()
    BasicAlertDialog(
        onDismissRequest = overlayNavController::overlayBack,
        content = {
            Column {
                Text("Edit profile")
                Button(onClick = overlayNavController::overlayBack) {
                    Text("Close")
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
val DeleteAccountSheet by navDestination {
    val overlayNavController = navController()
    ModalBottomSheet(onDismissRequest = overlayNavController::overlayBack) {
        Column {
            Text("Delete this account?")
            Button(onClick = overlayNavController::overlayBack) {
                Text("Cancel")
            }
        }
    }
}
```

Inside an overlay destination, `navController()` resolves to the local overlay controller. If you need the parent/root controller, use `overlayNavController.parent`.

## Closing overlays

Use `overlayBack()` to dismiss the current overlay. Unlike a normal parent back navigation, it is scoped to the overlay stack: it removes the last overlay entry when one exists, and if the overlay stack is already empty it simply clears the local overlay layer instead of navigating the parent/root controller back.

```kotlin
val overlays = ext<OverlaysExtension>() ?: error("OverlaysExtension is missing")
val overlayNavController = overlays.overlayNavController()

Button(onClick = overlayNavController::overlayBack) {
    Text("Close")
}
```

This makes `overlayBack()` the standard dismiss action for a modal flow in the local overlay host: it closes the current overlay without unexpectedly leaving the host destination.

## Configuration

The array constructor is shorthand for `DestinationLoader.from(destinations)`. Use the primary constructor when destinations must be resolved dynamically:

```kotlin
val overlays = OverlaysExtension(
    destinationLoader = DestinationLoader.byKey { key ->
        overlayDestinations.firstOrNull { it.key == key }
    },
    handleSystemBackEvents = false,
    overlaysNavControllerFactory = {
        rememberNavController(
            key = "settings-overlays",
            saveable = false,
        )
    },
)
```

- Set `handleSystemBackEvents = false` when the containing UI owns system-back handling.
- Use `overlaysNavControllerFactory` to customize creation of the local controller; the default controller is saveable and uses the key `OverlaysExtensionNavController`.

## Notes

- `OverlaysExtension` is attached to the host destination; it does not replace the destination.
- Overlay destinations can open other overlays or root screens without losing the host content beneath them.
- `ext<OverlaysExtension>()?.overlayNavController()` is the usual access point from inside a host destination.
