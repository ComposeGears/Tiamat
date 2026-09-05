# Overlay Destinations

Use the `tiamat-overlay` module for a dedicated local overlay stack.

## Setup

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("io.github.composegears:tiamat-overlay:$version")
    }
}
```

## How it works

Attach `OverlaysExtension` to a host destination. It creates a local `NavController` that is rendered on top of the host screen, while the host content stays visible underneath. Overlay destinations are regular `navDestination` entries that are opened from that local controller and dismissed with `overlayBack()`.

```kotlin
import androidx.compose.material3.BasicAlertDialog
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.overlay.OverlaysExtension
import com.composegears.tiamat.overlay.overlayBack

val HostScreen by navDestination(
    OverlaysExtension(destinations = arrayOf(EditProfileDialog))
) {
    val overlayNavController = ext<OverlaysExtension>()?.overlayNavController()
        ?: error("OverlaysExtension is missing")

    Column {
        Text("Settings")
        Button(onClick = { overlayNavController.navigate(EditProfileDialog) }) {
            Text("Edit profile")
        }
    }
}

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
```

### Common pattern

- Add the `tiamat-overlay` dependency.
- Attach `OverlaysExtension(destinations = arrayOf(...))` to the host destination.
- Access the local overlay controller with `ext<OverlaysExtension>()?.overlayNavController()`.
- Open overlay destinations via `overlayNavController.navigate(...)`.
- Dismiss the current overlay with `overlayNavController::overlayBack` or `NavController.overlayBack()`; this removes the last overlay entry and clears the overlay stack when it is already empty, instead of navigating the parent back.

### Configuration

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

### Key points

- The host content remains visible while overlays are open.
- Overlay destinations can open each other or navigate back to the root screen.
- `overlayNavController.parent` gives you the parent/root controller when needed.
- This pattern is ideal for dialogs, bottom sheets, and nested modal flows.
