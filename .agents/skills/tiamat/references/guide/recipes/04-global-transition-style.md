# Global Transition Style

Override the default animation for an entire `Navigation` host.

## How it works

Pass a `contentTransformProvider` lambda to `Navigation`. It receives an `isForward` boolean and returns a `ContentTransform`.

```kotlin
import com.composegears.tiamat.compose.Navigation
import com.composegears.tiamat.compose.navigationSlideInOut

Navigation(
    navController = nc,
    destinations = arrayOf(HomeScreen, DetailsScreen),
    contentTransformProvider = { isForward -> navigationSlideInOut(isForward) }
)
```

Built-in helpers: `navigationFadeInOut()` (default), `navigationSlideInOut(isForward)`, `navigationSlideInFromBottom()`, `navigationSlideOutToBottom()`, `navigationNone()`, `navigationPlatformDefault(isForward)`.

