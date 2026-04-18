# Route API

Build a multi-step back stack in one call or navigate by destination name.

## How it works

The `route` builder lets you construct an entire stack at once — useful for deep links or wizard flows.

```kotlin
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.navigation.NavController

@OptIn(TiamatExperimentalApi::class)
fun openWizardAtStep3(nc: NavController) {
    nc.route {
        element(WizardStep1)
        element(WizardStep2)
        element(WizardStep3)    // user lands here; Step1 and Step2 are in the back stack
    }
}
```

Navigate by destination name (useful for deep links or cross-module routing):

```kotlin
@OptIn(TiamatExperimentalApi::class)
fun openByDeepLink(nc: NavController) {
    nc.route {
        destination("HomeScreen")
        destination("OrderListScreen")
        destination("OrderDetailScreen")
    }
}
```

### Notes

- `route { destination("ByName") }` requires the named destination to be registered in the `destinations` array (or graph) — the compiler cannot verify this.

