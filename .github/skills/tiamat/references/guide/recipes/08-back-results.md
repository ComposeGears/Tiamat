# Back Results

Pass typed results back to the previous destination.

## How it works

Call `nc.back(result = value)` from the child destination. The parent reads it with `navResult<T>()`.

```kotlin
import com.composegears.tiamat.compose.back
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.compose.navResult

val EditProfileScreen by navDestination {
    val nc = navController()
    Button(onClick = { nc.back(result = "saved") }) {
        Text("Save and close")
    }
}

val ProfileScreen by navDestination {
    val saveResult = navResult<String>()
    Text("Last result: ${saveResult ?: "none"}")
}
```

### Notes

- `navResult<T>()` is captured via `remember` at composition time — it is not reactive state.
- Clear after consuming if needed: `clearNavResult()`.

