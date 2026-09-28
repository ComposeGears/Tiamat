# Produce Retained State

Coroutine-driven retained state with `produceRetainedState`.

## How it works

Like Compose's `produceState`, but the coroutine keeps running while the `NavEntry` is in the back stack (even when not visible) and is cancelled only when the entry is popped.

```kotlin
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.compose.produceRetainedState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(TiamatExperimentalApi::class)
val LiveFeedScreen by navDestination {
    val tickCount by produceRetainedState(0) {
        while (isActive) {
            delay(1000)
            value++
        }
    }
    Text("Ticks: $tickCount")
}
```

### Notes

- `produceRetainedState` is annotated `@TiamatExperimentalApi`; add `@OptIn(TiamatExperimentalApi::class)` at the call site.
- Only works inside a `navDestination {}` body.

