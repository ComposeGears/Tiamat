# Edit Nav Stack

Arbitrary stack manipulation with `editNavStack`.

## How it works

`editNavStack` gives you full control over the back stack entries.

```kotlin
import com.composegears.tiamat.compose.editNavStack
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry

// Clear history — keep only the current screen
nc.editNavStack { old -> old.takeLast(1) }

// Insert a screen just before the current one
nc.editNavStack { old ->
    val current = old.last()
    old.dropLast(1) + SomeScreen.toNavEntry() + current
}

// Apply a backward transition type when removing the last entry
nc.editNavStack(transitionType = NavController.TransitionType.Backward) { old ->
    old.dropLast(1)
}
```

