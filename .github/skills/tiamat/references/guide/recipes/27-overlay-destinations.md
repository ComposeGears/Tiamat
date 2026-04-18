# Overlay Destinations

Dialogs and bottom sheets rendered in the nav stack.

## How it works

Model overlays as regular destinations identified by a marker extension. Use `NavigationScene` to split the stack into a "content" layer and an "overlay" layer rendered on top. Overlay destinations handle their own dismiss logic (e.g., `onDismissRequest = nc::back`).

The key idea is:
1. Create a marker `NavExtension` to tag overlay destinations.
2. Use `derivedStateOf` to split the stack into the last non-overlay entry (content) and trailing overlay entries.
3. Animate the content layer normally; render overlays on top without animation (each overlay manages its own appearance).

```kotlin
import com.composegears.tiamat.compose.NavExtension
import com.composegears.tiamat.compose.ext
import com.composegears.tiamat.navigation.NavEntry

// Marker extension — no UI, just identifies overlay entries
class OverlayExtension<T : Any> : NavExtension<T> {
    companion object {
        fun NavEntry<*>.isOverlay(): Boolean =
            destination.ext<OverlayExtension<*>>() != null
    }
}
```

### Define overlay destinations

Attach `OverlayExtension()` to any destination that should render as an overlay. The destination's composable body contains the overlay UI (bottom sheet, dialog, etc.).

```kotlin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import com.composegears.tiamat.compose.back
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.compose.navigate

// A regular (non-overlay) screen
val MainScreen by navDestination<Unit> {
    val nc = navController()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { nc.navigate(MyBottomSheet) }) { Text("Open Bottom Sheet") }
        Button(onClick = { nc.navigate(MyDialog) }) { Text("Open Dialog") }
    }
}

// Bottom sheet overlay
@OptIn(ExperimentalMaterial3Api::class)
val MyBottomSheet by navDestination<Unit>(OverlayExtension()) {
    val nc = navController()
    ModalBottomSheet(onDismissRequest = nc::back) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Sheet content")
            Button(onClick = { nc.back() }) { Text("Close") }
        }
    }
}

// Dialog overlay
val MyDialog by navDestination<Unit>(OverlayExtension()) {
    val nc = navController()
    AlertDialog(
        onDismissRequest = { nc.back() },
        text = { Text("Dialog content") },
        confirmButton = {
            Button(onClick = { nc.navigate(MainScreen) }) { Text("Open Screen") }
        },
        dismissButton = {
            Button(onClick = { nc.back() }) { Text("Back") }
        },
    )
}
```

### Wire it up with `NavigationScene`

```kotlin
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.*
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavController

@Composable
fun OverlayHost() {
    val nc = rememberNavController(
        key = "overlay-nav",
        startDestination = MainScreen,
    )
    val stack by nc.navStackAsState()

    // Split into content + overlays
    val content by remember(stack) {
        derivedStateOf { stack.lastOrNull { !it.isOverlay() } }
    }
    val overlays by remember(stack) {
        derivedStateOf { stack.takeLastWhile { it.isOverlay() } }
    }

    NavigationScene(
        navController = nc,
        destinations = arrayOf(MainScreen, MyBottomSheet, MyDialog),
    ) {
        // Animate main content with slide transitions
        AnimatedContent(
            targetState = content,
            contentKey = { it?.contentKey() },
            transitionSpec = {
                navigationSlideInOut(
                    nc.navStateFlow.value.transitionType == NavController.TransitionType.Forward
                )
            },
        ) {
            CompositionLocalProvider(
                LocalNavAnimatedVisibilityScope provides this,
            ) {
                key(overlays) {
                    EntryContent(it)
                }
            }
        }
        // Draw all overlays on top — each manages its own animation
        key(overlays) {
            Box {
                for (entry in overlays) {
                    EntryContent(entry)
                }
            }
        }
    }
}
```

### Key points

- Overlays can open other overlays or regular screens — the stack handles nesting naturally.
- `key(overlays)` around `EntryContent` ensures content recomposes when the overlay list changes (e.g., to dim the background).
- Use `CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this)` inside `AnimatedContent` to enable shared-element transitions in content destinations.
- The `isOverlay()` check uses the companion function pattern for clean call-site syntax: `import ...OverlayExtension.Companion.isOverlay`.
