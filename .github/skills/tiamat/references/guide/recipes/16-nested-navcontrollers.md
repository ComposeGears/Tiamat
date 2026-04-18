# Nested NavControllers

Navigate across nested NavController hierarchies.

## How it works

`findParentNavController(key)` walks up the controller hierarchy until it finds one with the given `key`. Use it to escape a nested flow and trigger navigation in a parent controller.

```kotlin
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navigate

val nc = navController()

// Climb to the named root controller and navigate there
nc.findParentNavController("root-nav")?.navigate(MainMenuScreen)
```

