# Screen-Scoped ViewModel

Scope a ViewModel to a single screen.

## How it works

Standard `viewModel()` is scoped to the `NavEntry`. It is created the first time the screen appears and cleared when the entry is removed from the stack.

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

