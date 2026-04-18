# Observe NavController State

Drive UI from reactive NavController state.

## How it works

Use state-observation extensions to build reactive UI like back-button visibility, tab highlights, or breadcrumbs.

```kotlin
import com.composegears.tiamat.compose.canNavigateBackAsState
import com.composegears.tiamat.compose.currentNavDestinationAsState
import com.composegears.tiamat.compose.currentNavEntryAsState
import com.composegears.tiamat.compose.navStackAsState

val stack        by nc.navStackAsState()                  // full back stack
val currentEntry by nc.currentNavEntryAsState()           // current NavEntry or null
val activeTab    by nc.currentNavDestinationAsState()     // current NavDestination or null
val canGoBack    by nc.canNavigateBackAsState()           // true when stack size > 1
```

### Navigation listener (analytics / logging)

```kotlin
nc.setOnNavigationListener { from, to, type ->
    analytics.track(
        "navigation",
        mapOf("from" to from?.destination?.name, "to" to to?.destination?.name)
    )
}
```

