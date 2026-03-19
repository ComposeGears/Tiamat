---
name: Tiamat Arguments And Back Results
description: Build typed argument flows with `navDestination<Args>`, pass freeArgs for transient data, use NavData for persistent serializable args, and send typed results back to the previous destination.
---

# Goal

Use this skill when an agent needs typed input/output flow across destinations.

# Recipe: Destination with required args (`navArgs()`)

```kotlin
import androidx.compose.material3.Text
import com.composegears.tiamat.compose.navArgs
import com.composegears.tiamat.compose.navDestination

data class ItemArgs(val id: Int, val title: String)

val ItemDetailsScreen by navDestination<ItemArgs> {
    val args = navArgs()           // throws clearly if args are absent
    Text("Item: ${args.id} – ${args.title}")
}
```

Use `navArgsOrNull()` when args are optional (e.g., the destination can also be the `startDestination`):

```kotlin
val itemId = navArgsOrNull()?.id ?: -1
```

# Recipe: Navigate and pass args

```kotlin
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navDestination
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

# Recipe: Pass lightweight transient data with `freeArgs`

`freeArgs` - use it for transient data (e.g., a pre-loaded model, a parsed deep-link context).

```kotlin
import com.composegears.tiamat.compose.freeArgs
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.compose.navigate

data class DeepLinkContext(val source: String, val campaignId: String)

val LandingScreen by navDestination {
    val ctx = freeArgs<DeepLinkContext>()   // null if not provided or wrong type
}

// caller:
nc.navigate(LandingScreen, freeArgs = DeepLinkContext("email", "spring-sale"))
```

# Recipe: Serializable args with `NavData` (survive process death)

Annotate the args class with `@Serializable` and implement `NavData`. Tiamat will encode them into `SavedState` automatically so they survive process death and `NavController.saveToSavedState()` / restore cycles.
Primitive types and collections of primitives are supported out of the box. 

```kotlin
import com.composegears.tiamat.navigation.NavData
import kotlinx.serialization.Serializable

@Serializable
data class OrderArgs(val orderId: Long, val status: String) : NavData
```

Use it exactly like a plain data class:

```kotlin
val OrderDetailScreen by navDestination<OrderArgs> {
    val args = navArgs()    // automatically decoded after process restore
    Text("Order #${args.orderId} – ${args.status}")
}
```

> **Important:** Apply the Kotlin serialization plugin to the module and ensure
> `kotlinx-serialization-json` (or another format) is on the classpath.

# Recipe: Pass data back to previous destination

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

# Notes For Agents

- `navArgs()`, `navArgsOrNull()`, `freeArgs<T>()`, and `navResult<T>()` are captured via `remember` at composition time — they are **not** reactive state. Copy into `mutableStateOf(...)` if the screen needs to react to the value after initial composition.
- Clear after consuming if needed to prevent stale data on back navigation: `clearNavArgs()`, `clearFreeArgs()`, `clearNavResult()`.
- Use `navArgs()` (non-null) when the destination is always reached via `navigate(..., navArgs = ...)`. Use `navArgsOrNull()` when the destination may also be the `startDestination` with no args.
- `freeArgs` is a transient and not designed to be saved in the back stack or survive process death yet capable to if this required.
