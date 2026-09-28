# Navigate Between Screens

Perform forward and back navigation between destinations.

## How it works

Inside a `navDestination` body, call `navController()` to get the current `NavController`. Use `navigate(destination)` to go forward and `back()` to go back.

```kotlin
import com.composegears.tiamat.compose.back
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.compose.navigate

val HomeScreen by navDestination {
    val nc = navController()
    Button(onClick = { nc.navigate(DetailsScreen) }) {
        Text("Open details")
    }
}

val DetailsScreen by navDestination {
    val nc = navController()
    Button(onClick = { nc.back() }) {
        Text("Back")
    }
}
```

