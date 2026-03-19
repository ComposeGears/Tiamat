---
name: Tiamat Custom Layouts and Transitions
description: Use `NavigationScene` for full layout control (two-pane, overlays, adaptive), `TransitionController` for gesture/seekable transitions, and shared-element transitions.
---

# Goal

Use this skill when the standard `Navigation` composable is not flexible enough — custom split-pane layouts, overlay destinations (dialogs/sheets rendered in the nav stack), gesture-driven transitions, or any scenario where you need to decide *where* each stack entry renders.

# Recipe: `NavigationScene` — full layout control

`NavigationScene` provides a `NavigationSceneScope` with a single `EntryContent(entry)` composable. It is responsible for back-handling and destination loading; you supply the animation and layout.

```kotlin
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.getValue
import com.composegears.tiamat.compose.NavigationScene
import com.composegears.tiamat.compose.currentNavEntryAsState
import com.composegears.tiamat.compose.navigationFadeInOut
import com.composegears.tiamat.navigation.NavEntry

NavigationScene(
    navController = nc,
    destinations = arrayOf(HomeScreen, DetailsScreen),
) { // this: NavigationSceneScope
    val currentEntry by nc.currentNavEntryAsState()
    AnimatedContent(
        targetState = currentEntry,
        contentKey = { it?.contentKey() },
        transitionSpec = { navigationFadeInOut() },
    ) { entry: NavEntry<*>? ->
        EntryContent(entry)   // EntryContent(null) is a no-op
    }
}
```

# Recipe: Overlay destinations (dialogs / bottom sheets in the nav stack)

Model overlays as regular destinations identified by a marker extension. `NavigationScene` splits the stack into a "content" layer and an "overlay" layer rendered on top.

```kotlin
import com.composegears.tiamat.compose.NavExtension
import com.composegears.tiamat.compose.back
import com.composegears.tiamat.compose.ext
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.navigation.NavEntry

// Marker — no UI, just identifies overlay entries
class OverlayExtension<T : Any> : NavExtension<T>
fun NavEntry<*>.isOverlay() = destination.ext<OverlayExtension<*>>() != null

// Attach the marker to an overlay destination
val MyBottomSheet by navDestination<Unit>(OverlayExtension()) {
    val nc = navController()
    ModalBottomSheet(onDismissRequest = nc::back) { /* sheet content */ }
}

// In the host:
NavigationScene(
    navController = nc,
    destinations = arrayOf(MainScreen, MyBottomSheet),
) {
    val stack by nc.navStackAsState()
    val content  by remember(stack) { derivedStateOf { stack.lastOrNull { !it.isOverlay() } } }
    val overlays by remember(stack) { derivedStateOf { stack.takeLastWhile { it.isOverlay() } } }

    // Animate the main content layer
    AnimatedContent(
        targetState = content,
        contentKey = { it?.contentKey() },
        transitionSpec = { navigationFadeInOut() },
    ) { EntryContent(it) }

    // Draw all overlays on top, without animation (each handles its own)
    Box {
        for (entry in overlays) EntryContent(entry)
    }
}
```

# Recipe: Two-pane / list-detail layout

```kotlin
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.composegears.tiamat.compose.NavigationScene
import com.composegears.tiamat.compose.navStackAsState
import com.composegears.tiamat.compose.navigationFadeInOut

NavigationScene(
    navController = nc,
    destinations = arrayOf(ListScreen, DetailScreen),
) {
    Row {
        val stack by nc.navStackAsState()
        val listEntry   by remember(stack) { derivedStateOf { stack.firstOrNull() } }
        val detailEntry by remember(stack) { derivedStateOf { stack.lastOrNull()?.takeIf { stack.size > 1 } } }

        Box(Modifier.weight(1f).fillMaxHeight()) {
            EntryContent(listEntry)
        }
        AnimatedContent(
            targetState = detailEntry,
            contentKey = { it?.contentKey() },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            transitionSpec = { navigationFadeInOut() },
        ) {
            EntryContent(it)
        }
    }
}
```

# Recipe: `TransitionController` — gesture/seekable transitions

Create a `TransitionController`, pass it to `navigate` (or `replace` / `back`), then drive progress manually. Once `finish()` or `cancel()` is called the controller becomes inactive — create a new instance for each gesture.

```kotlin
import com.composegears.tiamat.compose.TransitionController

// Start a gesture-driven transition
val controller = TransitionController()
nc.navigate(NextScreen, transitionController = controller)

// As the gesture progresses (0.0 → 1.0)
controller.update(gestureProgress)

// Gesture committed — animate to completion
controller.finish()

// Gesture cancelled — snap back
controller.cancel()
```

# Recipe: Shared-element transitions

Wrap the `Navigation` (or `NavigationScene`) host in `SharedTransitionLayout` and consume `LocalNavAnimatedVisibilityScope` inside each destination.

```kotlin
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import com.composegears.tiamat.compose.LocalNavAnimatedVisibilityScope
import com.composegears.tiamat.compose.Navigation

@OptIn(ExperimentalSharedTransitionApi::class)
SharedTransitionLayout {
    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
        Navigation(
            navController = nc,
            destinations = arrayOf(ListScreen, DetailScreen),
            contentTransformProvider = { isForward -> navigationSlideInOut(isForward) }
        )
    }
}

// Inside a destination:
val DetailScreen by navDestination<String> {
    with(LocalSharedTransitionScope.current) {
        with(LocalNavAnimatedVisibilityScope.current!!) {
            Image(
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState("hero-image"),
                    animatedVisibilityScope = this,
                )
            )
        }
    }
}
```

# Notes For Agents

- `NavigationScene` does not provide a default animation — the agent must wrap `EntryContent` in `AnimatedContent` (or equivalent) to get transitions.
- `contentKey = { it?.contentKey() }` is required in `AnimatedContent` so Compose treats each `NavEntry` instance as unique and animates correctly between them.
- `EntryContent(null)` is a safe no-op; checking for null before calling is unnecessary.
- Set `handleSystemBackEvent = false` on `NavigationScene` for secondary panes that should not intercept the system back gesture.
- `TransitionController` becomes inactive after `finish()` or `cancel()` — calling any method afterwards throws. Always create a fresh instance per gesture.
- `LocalNavAnimatedVisibilityScope` is `null` when used outside a `Navigation`/`NavigationScene` host (e.g., inside `TiamatPreview`). Guard with `?.` or `!!` only when the context guarantees it.

