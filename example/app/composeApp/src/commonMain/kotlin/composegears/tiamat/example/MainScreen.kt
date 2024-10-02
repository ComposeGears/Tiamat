package composegears.tiamat.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import composegears.tiamat.example.multimodule.MultiModuleRoot
import composegears.tiamat.sample.koin.KoinIntegrationScreen

val MainScreen by navDestination<Unit> {
    val navController = navController()
    val content = remember {
        listOf(
            "Simple forward/back" to { navController.navigate(SimpleForwardBackRoot) },
            "Simple replace & circular screen dependencies" to { navController.navigate(SimpleReplaceRoot) },
            "NavigationBar + custom back handling" to { navController.navigate(SimpleTabsRoot) },
            "Nested navigation" to { navController.navigate(NestedNavigationRoot) },
            "Data passing: params" to { navController.navigate(DataPassingParamsRoot) },
            "Data passing: free args" to { navController.navigate(DataPassingFreeArgsRoot) },
            "Data passing: result" to { navController.navigate(DataPassingResultRoot) },
            "ViewModels" to { navController.navigate(ViewModelsRoot) },
            "Koin (ViewModels)" to { navController.navigate(KoinIntegrationScreen) },
            "Custom transition" to { navController.navigate(CustomTransitionRoot) },
            "Custom state saver" to { navController.navigate(CustomStateSaverRoot) },
            "Multi-module" to { navController.navigate(MultiModuleRoot) },
            "Back stack alteration" to { navController.navigate(BackStackAlterationRoot) },
            "2 Pane (list + detail, resizable)" to { navController.navigate(TwoPaneResizableRoot) },
            "Platform specific" to { navController.navigate(PlatformExamplesScreen) }
        )
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(content) { (name, action) ->
                Button(
                    modifier = Modifier.widthIn(max = 450.dp),
                    onClick = action,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(modifier = Modifier.weight(1f), text = name)
                        Icon(Icons.AutoMirrored.Filled.NavigateNext, "")
                    }
                }
            }
        }
    }
}