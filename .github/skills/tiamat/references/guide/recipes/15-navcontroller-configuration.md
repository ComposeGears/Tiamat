# NavController Configuration

Apply a configuration callback to a NavController at creation time.

## How it works

`rememberNavController` accepts a `configuration` lambda that runs on the NavController after creation. Use it to set up listeners or other one-time setup.

```kotlin
import com.composegears.tiamat.compose.rememberNavController

val nc = rememberNavController(
    key = "rootNavController",
    startDestination = HomeScreen,
    configuration = {
        setOnNavigationListener { from, to, type ->
            analytics.trackNavigation(from?.destination?.name, to?.destination?.name)
        }
    }
)
```

This is also useful when the configuration is passed from a platform-specific layer:

```kotlin
@Composable
fun App(navControllerConfig: NavController.() -> Unit = {}) {
    val nc = rememberNavController(
        startDestination = HomeScreen,
        configuration = navControllerConfig,
    )
    Navigation(navController = nc, destinations = arrayOf(HomeScreen, DetailsScreen))
}
```

