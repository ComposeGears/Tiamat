# Preview Destinations

Render any destination inside a Compose `@Preview` with `TiamatPreview`.

## How it works

`TiamatPreview` renders a destination without a real navigation stack. It accepts optional `navArgs`, `freeArgs`, and `navResult` parameters to test different states.

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.composegears.tiamat.compose.TiamatPreview

@Preview
@Composable
private fun HomeScreenPreview() {
    TiamatPreview(destination = HomeScreen)
}
```

For destinations with typed args:

```kotlin
data class ItemArgs(val id: Int, val title: String)

val ItemScreen by navDestination<ItemArgs> { /* ... */ }

@Preview
@Composable
private fun ItemScreenPreview() {
    TiamatPreview(
        destination = ItemScreen,
        navArgs = ItemArgs(id = 1, title = "Preview item"),
    )
}
```

`TiamatPreview` also accepts `freeArgs` and `navResult` to test those states in isolation.

