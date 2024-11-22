package composegears.tiamat.example.content.content

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.NavDestination
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import composegears.tiamat.example.ui.core.FillSpace
import composegears.tiamat.example.ui.core.LocalThemeConfig
import composegears.tiamat.example.ui.core.VSpacer

private val HomeItems = listOf(
    HomeItem(
        "Navigation",
        listOf(
            HomeItemDestination(
                name = "Forward & back",
                description = "Simple navigation back & forward case",
                destination = ::StubScreen
            ),
            HomeItemDestination(
                name = "Replace",
                description = "Replace (navigate without adding current destination to back stack) case",
                destination = ::StubScreen
            ),
            HomeItemDestination(
                name = "Nested navigation",
                description = "Multiple nested nav controllers case",
                destination = ::StubScreen
            ),
            HomeItemDestination(
                name = "Custom animation",
                description = "On fly customizable navigation animation",
                destination = ::StubScreen
            ),
            HomeItemDestination(
                name = "Tabs navigation",
                description = "Simple tab's navigation with a separate nav controllers for each tab",
                destination = ::StubScreen
            ),
            HomeItemDestination(
                name = "Routing",
                description = "Advanced Route-Api demo (building nav-path)",
                destination = ::StubScreen
            ),
        ),
    ),
    HomeItem(
        "Args / Params / Result",
        listOf(
            HomeItemDestination(
                name = "NavArgs",
                description = "Passing navigation-arguments to next screen example",
                destination = ::StubScreen
            ),
            HomeItemDestination(
                name = "FreeArgs",
                description = "Passing free-type-arguments to next screen example",
                destination = ::StubScreen
            ),
            HomeItemDestination(
                name = "NavResult",
                description = "Returning result tp previous screen",
                destination = ::StubScreen
            ),
        ),
    ),
    HomeItem(
        "Architecture",
        listOf(
            HomeItemDestination(
                name = "ViewModel",
                description = "ViewModel usage demo",
                destination = ::StubScreen
            ),
            HomeItemDestination(
                name = "Custom SaveState",
                description = "Custom save and restore state logic case",
                destination = ::StubScreen
            ),
            HomeItemDestination(
                name = "Back stack alteration",
                description = "Editing back stack on the fly example",
                destination = ::StubScreen
            ),
        ),
    ),
    HomeItem(
        "Advanced examples",
        listOf(
            HomeItemDestination(
                name = "Two Pane navigation",
                description = "Resizable mobile/desktop example of 2-pane navigation",
                destination = ::StubScreen
            ),
        ),
    ),
)

val StubScreen by navDestination<Unit> {
    Text("Stub")
}

val HomeScreen by navDestination<Unit> {
    val navController = navController()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var selectedItem by rememberSaveable { mutableStateOf<String?>(null) }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Text("Tiamat", style = MaterialTheme.typography.headlineMedium) }
            items(HomeItems) {
                HomeGroupItem(
                    item = it,
                    isExpanded = selectedItem == it.title,
                    onItemClick = { item -> selectedItem = item.title },
                    onDestinationSelect = { dest -> navController.navigate(dest) },
                )
            }
        }
        val themeConfig = LocalThemeConfig.current
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = { themeConfig.isDarkMode = !themeConfig.isDarkMode }) {
            Icon(Icons.Default.DarkMode, "")
        }
    }
}

@Composable
private fun HomeGroupItem(
    item: HomeItem,
    isExpanded: Boolean,
    onItemClick: (HomeItem) -> Unit,
    onDestinationSelect: (NavDestination<*>) -> Unit,
) {
    val indicatorColor by animateColorAsState(
        if (isExpanded) MaterialTheme.colorScheme.primary else Color.Transparent
    )
    val iconRotation by animateFloatAsState(if (isExpanded) 0f else 180f)

    Surface(
        modifier = Modifier
            .animateContentSize()
            .widthIn(max = 400.dp)
            .height(IntrinsicSize.Min),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.fillMaxWidth()) {
            // header / title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isExpanded) { onItemClick(item) }
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(item.title, Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                FillSpace()
                Icon(Icons.Default.KeyboardArrowUp, "", Modifier.rotate(iconRotation))
            }
            // items
            if (isExpanded) item.items.onEach {
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDestinationSelect(it.destination()) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.padding(16.dp).weight(1f)) {
                        Text(it.name, style = MaterialTheme.typography.titleSmall)
                        if (it.description.isNotBlank()) {
                            VSpacer(4.dp)
                            Text(it.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "")
                }
            }
        }
        // selected group indicator
        Box(Modifier.fillMaxHeight().width(3.dp).background(indicatorColor))
    }
}

private data class HomeItem(
    val title: String,
    val items: List<HomeItemDestination>
)

private data class HomeItemDestination(
    val name: String,
    val description: String,
    val destination: () -> NavDestination<*>
)
