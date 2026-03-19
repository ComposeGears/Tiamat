---
name: Tiamat ViewModel and State
description: Scope ViewModels to a screen or NavController, retain objects across backward navigation with `retain {}`, and use `produceRetainedState` for coroutine-driven retained state.
---

# Goal

Use this skill when an agent needs state that survives recomposition, configuration changes, or backward navigation (back-stack retention).

For state that must survive **process death**, use `NavData` args (see `tiamat-02-arguments-and-results`) or `SavedStateHandle` inside a ViewModel.

# Recipe: Screen-scoped ViewModel

Standard Jetpack `viewModel()` is scoped to the `NavEntry`. It is created the first time the screen appears and cleared when the entry is removed from the stack.

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composegears.tiamat.compose.navDestination

class CounterViewModel : ViewModel() {
    var count = 0
}

val CounterScreen by navDestination {
    val vm = viewModel { CounterViewModel() }
    // vm.count survives recomposition and configuration changes
}
```

# Recipe: NavController-scoped shared ViewModel

Pass the `NavController` as the `ViewModelStoreOwner` to share a single ViewModel instance across all destinations inside that controller. The ViewModel is cleared when the NavController itself is destroyed.

```kotlin
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navDestination

val Screen1 by navDestination {
    val nc = navController()
    val shared = viewModel(nc) { SharedViewModel() }
    // shared is the same instance in every destination under nc
}

val Screen2 by navDestination {
    val nc = navController()
    val shared = viewModel(nc) { SharedViewModel() }
}
```

# Recipe: `retain {}` — lightweight retained object

`retain {}` is an alternative to `remember {}`. The retained object lives as long as the `NavEntry` is anywhere in the back stack, so it survives the user navigating forward to a child screen and returning.

```kotlin
import androidx.compose.runtime.retain.retain
import com.composegears.tiamat.compose.navDestination

val FormScreen by navDestination {
    // FormState is kept alive when the user goes forward and comes back
    val formState = retain { FormState() }
}
```

`retain` supports keys just like `remember`:

```kotlin
val filtered = retain(userId) { FilteredList(userId) }
```

# Recipe: `produceRetainedState` — retained coroutine-driven state

Like Compose's `produceState`, but the coroutine keeps running while the `NavEntry` is in the back stack (even when not visible) and is cancelled only when the entry is popped off the stack.

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
    // tickCount keeps incrementing in the background while the user visits child screens
    Text("Ticks: $tickCount")
}
```

# Recipe: ViewModel with `SavedStateHandle` (survives process death)

```kotlin
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel

class FormViewModel(val savedState: SavedStateHandle) : ViewModel()

val FormScreen by navDestination {
    val vm = viewModel { FormViewModel(createSavedStateHandle()) }
}
```

# Recipe: `saveable` parameter on `rememberNavController`

Control whether the back-stack of a NavController is preserved across process death.

```kotlin
// Persists across process death (default)
val nc = rememberNavController(startDestination = HomeScreen)

// Ephemeral — not restored after process death (e.g., modal sub-flows)
val nc = rememberNavController(
    startDestination = HomeScreen,
    saveable = false,
)
```

# Notes For Agents

- Prefer `retain {}` over `remember {}` for objects that must not reset when the user returns from a child screen.
- Prefer `ViewModel` when state is shared across destinations or needs `SavedStateHandle`.
- `produceRetainedState` is annotated `@TiamatExperimentalApi`; add `@OptIn(TiamatExperimentalApi::class)` at the call site.
- `retain {}` and `produceRetainedState` only work inside a `navDestination {}` body — they rely on `LocalRetainedValuesStore` provided by the navigation entry and will throw outside that context.
- `viewModel(nc) { ... }` with a `NavController` owner creates a ViewModel cleared when the entire navigation flow ends, not when a single screen exits.
