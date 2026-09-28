# Gesture Transitions

Gesture-driven / seekable transitions with `TransitionController`.

## How it works

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

### Notes

- `TransitionController` becomes inactive after `finish()` or `cancel()` — calling any method afterwards throws.
- Always create a fresh instance per gesture.

