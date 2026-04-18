# SavedStateHandle ViewModel

ViewModel with `SavedStateHandle` for process-death survival.

## How it works

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

### `saveable` parameter on `rememberNavController`

Control whether the back-stack is preserved across process death:

```kotlin
// Persists across process death (default)
val nc = rememberNavController(startDestination = HomeScreen)

// Ephemeral — not restored after process death
val nc = rememberNavController(
    startDestination = HomeScreen,
    saveable = false,
)
```

