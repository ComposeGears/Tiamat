# Hilt ViewModel

Use Hilt's `hiltViewModel` with Tiamat for dependency-injected ViewModels on Android.

## How it works

Tiamat destinations are standard `@Composable` scopes, so `hiltViewModel()` works out of the box. Annotate your ViewModel with `@HiltViewModel`, inject dependencies via `@Inject constructor`, and call `hiltViewModel()` inside a `navDestination` body.

### Setup

Annotate your `Application` class and `Activity` with Hilt:

```kotlin
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.AndroidEntryPoint

@HiltAndroidApp
class MyApp : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() { /* ... */ }
```

### Define a Hilt ViewModel and use it in a destination

```kotlin
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.composegears.tiamat.compose.navDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class MyRepository @Inject constructor() {
    fun getData() = "Hello from Hilt!"
}

@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel() {
    fun getMessage() = repository.getData()
}

val MyScreen by navDestination {
    val viewModel: MyViewModel = hiltViewModel()
    Text(viewModel.getMessage())
}
```

### Notes

- `hiltViewModel()` is Android-only — for multiplatform projects, use it in `androidMain` source sets or Android-specific modules.
- For multiplatform DI, see the [Koin ViewModel](koin-viewmodel.md) recipe instead.

