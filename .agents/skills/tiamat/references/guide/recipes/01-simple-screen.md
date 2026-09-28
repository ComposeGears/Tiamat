# Simple Screen

Create a screen with `navDestination` and wire it into a `Navigation` host.

## How it works

`navDestination` is a property delegate that wraps a `@Composable` lambda into a `ComposeNavDestination`. Pass it to a `Navigation` host along with a `NavController` to display it.

```kotlin
import androidx.compose.runtime.Composable
import com.composegears.tiamat.compose.Navigation
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.compose.rememberNavController

val HomeScreen by navDestination {
    Text("Welcome home")
}

val DetailsScreen by navDestination {
    Text("Details")
}

@Composable
fun AppNavigation() {
    val nc = rememberNavController(
        startDestination = HomeScreen,
    )
    Navigation(
        navController = nc,
        destinations = arrayOf(HomeScreen, DetailsScreen),
    )
}
```

