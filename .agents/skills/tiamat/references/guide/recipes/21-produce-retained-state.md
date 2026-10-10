# Produce Retained State

Coroutine-driven retained state with `produceRetainedState` and a retained coroutine scope with `retainCoroutineScope`.

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

## Retained coroutine scope

`retainCoroutineScope()` is the retained counterpart of Compose's `rememberCoroutineScope()`. The same scope instance is returned while the `NavEntry` is in the back stack, so coroutines launched from it (e.g. from click handlers) keep running when the user navigates forward and are cancelled only when the entry is popped.

```kotlin
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.retain.retain
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.compose.retainCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(TiamatExperimentalApi::class)
val UploadScreen by navDestination {
    val scope = retainCoroutineScope()
    var progress by retain { mutableIntStateOf(0) }
    Button(onClick = {
        scope.launch {
            repeat(100) {
                delay(100)
                progress++
            }
        }
    }) {
        Text("Upload ($progress%)")
    }
}
```

### Notes

- `produceRetainedState` and `retainCoroutineScope` are annotated `@TiamatExperimentalApi`; add `@OptIn(TiamatExperimentalApi::class)` at the call site.
- Only works inside a `navDestination {}` body.
- `produceRetainedState` and `retainCoroutineScope` use a `SupervisorJob` and `Dispatchers.Main.immediate` (falls back to the caller's context on platforms without `Dispatchers.Main`).
- Like `produceState`, `produceRetainedState(initialValue, keys)` restarts the producer when keys change (the previous one is canceled immediately), but does not reset the state to `initialValue`.
- Store values updated from a retained scope in `retain {}`-ed state, otherwise the UI loses them when the entry is recreated.
