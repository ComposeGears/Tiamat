# Serializable Args

Use `NavData` + `@Serializable` for args that survive process death.

## How it works

Annotate the args class with `@Serializable` and implement `NavData`. Tiamat encodes them into `SavedState` automatically so they survive process death and `NavController.saveToSavedState()` / restore cycles.

```kotlin
import com.composegears.tiamat.navigation.NavData
import kotlinx.serialization.Serializable

@Serializable
data class OrderArgs(val orderId: Long, val status: String) : NavData
```

Use it exactly like a plain data class:

```kotlin
val OrderDetailScreen by navDestination<OrderArgs> {
    val args = navArgs()
    Text("Order #${args.orderId} – ${args.status}")
}
```

> **Important:** Apply the Kotlin serialization plugin to the module and ensure `kotlinx-serialization-json` (or another format) is on the classpath. Primitive types and collections of primitives are supported out of the box.

