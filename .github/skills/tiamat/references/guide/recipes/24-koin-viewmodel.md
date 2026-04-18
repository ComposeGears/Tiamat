# Koin ViewModel

Use Koin's `koinViewModel` with Tiamat for dependency-injected ViewModels.

## How it works

Tiamat's `NavController` implements `ViewModelStoreOwner`, so you can pass it directly to `koinViewModel(viewModelStoreOwner = nc)` to share a ViewModel across all destinations in that controller.

### Setup

```kotlin
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::MyViewModel)
    viewModelOf(::SharedViewModel)
}

fun initKoin() {
    startKoin { modules(appModule) }
}
```

### Screen-scoped Koin ViewModel

```kotlin
import com.composegears.tiamat.compose.navDestination
import org.koin.compose.viewmodel.koinViewModel

val Screen1 by navDestination {
    val vm = koinViewModel<MyViewModel>()  // scoped to this screen's NavEntry
}
```

### NavController-scoped shared Koin ViewModel

```kotlin
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navDestination
import org.koin.compose.viewmodel.koinViewModel

val Screen1 by navDestination {
    val nc = navController()
    val shared = koinViewModel<SharedViewModel>(viewModelStoreOwner = nc)
}

val Screen2 by navDestination {
    val nc = navController()
    val shared = koinViewModel<SharedViewModel>(viewModelStoreOwner = nc)
    // same instance as Screen1
}
```

### Notes

- Koin's `koinViewModel` works identically to Jetpack's `viewModel` for scoping — the only difference is how the ViewModel is constructed (via Koin DI).
- Ensure `startKoin` is called before any composable that uses `koinViewModel`.

