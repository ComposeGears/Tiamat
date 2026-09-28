# Shared ViewModel

Share a ViewModel across all destinations under a NavController.

## How it works

Pass the `NavController` as the `ViewModelStoreOwner` to share a single ViewModel instance across all destinations inside that controller. The ViewModel is cleared when the NavController itself is destroyed.

```kotlin
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navDestination

val Screen1 by navDestination {
    val nc = navController()
    val shared = viewModel(nc) { SharedViewModel() }
}

val Screen2 by navDestination {
    val nc = navController()
    val shared = viewModel(nc) { SharedViewModel() }
    // same instance as Screen1
}
```

