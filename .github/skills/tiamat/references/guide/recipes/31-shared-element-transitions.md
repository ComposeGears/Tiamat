# Shared-Element Transitions

Shared-element transitions with `LocalNavAnimatedVisibilityScope`.

## How it works

Wrap the `Navigation` host in `SharedTransitionLayout` and consume `LocalNavAnimatedVisibilityScope` inside each destination.

```kotlin
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import com.composegears.tiamat.compose.LocalNavAnimatedVisibilityScope
import com.composegears.tiamat.compose.Navigation
import com.composegears.tiamat.compose.navigationSlideInOut

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

### Notes

- `LocalNavAnimatedVisibilityScope` is `null` when used outside a `Navigation`/`NavigationScene` host (e.g., inside `TiamatPreview`). Guard with `?.`.

