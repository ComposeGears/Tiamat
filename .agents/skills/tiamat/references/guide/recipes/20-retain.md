# Retain

Retain objects across backward navigation with `retain {}`.

## How it works

`retain {}` is an alternative to `remember {}`. The retained object lives as long as the `NavEntry` is anywhere in the back stack, so it survives the user navigating forward and returning.

```kotlin
import androidx.compose.runtime.retain.retain
import com.composegears.tiamat.compose.navDestination

val FormScreen by navDestination {
    val formState = retain { FormState() }
}
```

`retain` supports keys just like `remember`:

```kotlin
val filtered = retain(userId) { FilteredList(userId) }
```

### Notes

- Prefer `retain {}` over `remember {}` for objects that must not reset when the user returns from a child screen.
- Values that leave the composition while the screen is displayed (key change, `if` branch turned off) are retired immediately.
- When `retain` is the last expression of a `Unit` lambda, Kotlin infers `Unit` as its type and the lambda result is dropped — assign it to a value or specify the type explicitly: `retain<Tracker> { Tracker() }`.
- `retain {}` only works inside a `navDestination {}` body — it relies on `LocalRetainedValuesStore` provided by the navigation entry.

