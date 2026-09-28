# Free Args

Pass lightweight transient data with `freeArgs`.

## How it works

`freeArgs` is for transient data (e.g., a pre-loaded model, a parsed deep-link context) that does not need to survive process death.

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

### Notes

- `freeArgs` is transient and not designed to be saved in the back stack or survive process death, yet capable to if required.
- Clear after consuming if needed: `clearFreeArgs()`.

