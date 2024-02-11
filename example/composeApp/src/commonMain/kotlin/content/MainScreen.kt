package content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import content.examples.*


val MainScreen by navDestination<Unit> {
    val navController = navController()
    val content = remember {
        listOf(
            "Simple forward/back" to { navController.navigate(SimpleForwardBackRoot) },
            "Simple replace" to { navController.navigate(SimpleReplaceRoot) },
            "BottomBar Tabs + custom back" to { navController.navigate(SimpleTabsRoot) },
            "Nested navigation" to { navController.navigate(NestedNavigationRoot) },
            "Data passing: params" to { navController.navigate(DataPassingParamsRoot) },
            "Data passing: result" to { navController.navigate(DataPassingResultRoot) },
            "View models" to { navController.navigate(ViewModelsRoot) },
            "Custom transition" to { navController.navigate(CustomTransitionRoot) }
        )
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
    ) {
        items(content) { (name, action) ->
            Column(Modifier) {
                Button(action, Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row {
                        Text(name, Modifier.weight(1f))
                        Icon(Icons.Default.ArrowForward, contentDescription = "")
                    }
                }
                Divider()
            }
        }
    }
}

