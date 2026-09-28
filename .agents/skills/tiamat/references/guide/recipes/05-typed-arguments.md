# Typed Arguments

Declare a destination with required or optional typed args.

## How it works

Parameterize `navDestination<Args>` with a data class. Inside the body, call `navArgs()` (throws if absent) or `navArgsOrNull()` (returns null). The caller passes args via `navigate(destination, navArgs = ...)`.

```kotlin
import com.composegears.tiamat.compose.navArgs
import com.composegears.tiamat.compose.navDestination

data class ItemArgs(val id: Int, val title: String)

val ItemDetailsScreen by navDestination<ItemArgs> {
    val args = navArgs()
    Text("Item: ${args.id} – ${args.title}")
}
```

Optional args (e.g., when the destination may also be a `startDestination`):

```kotlin
val itemId = navArgsOrNull()?.id ?: -1
```

Navigate and pass args:

```kotlin
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navigate

val ItemListScreen by navDestination {
    val nc = navController()
    Button(onClick = {
        nc.navigate(ItemDetailsScreen, navArgs = ItemArgs(id = 42, title = "Widget"))
    }) {
        Text("Open item")
    }
}
```

### Notes

- `navArgs()` and `navArgsOrNull()` are captured via `remember` at composition time — they are not reactive state. Copy into `mutableStateOf(...)` if the screen needs to react to the value after initial composition.
- Clear after consuming if needed: `clearNavArgs()`.

