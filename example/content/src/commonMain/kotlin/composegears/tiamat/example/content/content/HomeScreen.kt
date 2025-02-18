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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.NavDestination
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import composegears.tiamat.example.content.content.advanced.AdvBackStackAlteration
import composegears.tiamat.example.content.content.advanced.AdvExtensions
import composegears.tiamat.example.content.content.apr.APRFreeArgs
import composegears.tiamat.example.content.content.apr.APRNavArgs
import composegears.tiamat.example.content.content.apr.APRNavResult
import composegears.tiamat.example.content.content.architecture.ArchCustomSaveState
import composegears.tiamat.example.content.content.architecture.ArchViewModel
import composegears.tiamat.example.content.content.navigation.*
import composegears.tiamat.example.extra.A3rdParty
import composegears.tiamat.example.platform.Platform
import composegears.tiamat.example.platform.features
import composegears.tiamat.example.platform.name
import composegears.tiamat.example.ui.core.*

private val HomeItems =
    listOf(
        HomeItem(
            "Navigation",
            listOf(
                AppFeature(
                    name = "Forward & back",
                    description = "Simple navigation back & forward case",
                    destination = NavForwardAndBack
                ),
                AppFeature(
                    name = "Replace",
                    description = "Replace (navigate without adding current destination to back stack) case",
                    destination = NavReplace
                ),
                AppFeature(
                    name = "Nested navigation",
                    description = "Multiple nested nav controllers case",
                    destination = NavNested
                ),
                AppFeature(
                    name = "Custom animation",
                    description = "On fly customizable navigation animation",
                    destination = NavCustomAnimation
                ),
                AppFeature(
                    name = "Tabs navigation",
                    description = "Simple tab's navigation with a separate nav controllers for each tab." +
                        "Sample use `popToTop` nav action to bring to from previously opened screen",
                    destination = NavTabs
                ),
                AppFeature(
                    name = "Routing (experimental)",
                    description = "Advanced Route-Api demo (building nav-path)",
                    destination = NavRoute
                ),
            ),
        ),
        HomeItem(
            "Args / Params / Result",
            listOf(
                AppFeature(
                    name = "NavArgs",
                    description = "Passing navigation-arguments to next screen example",
                    destination = APRNavArgs
                ),
                AppFeature(
                    name = "FreeArgs",
                    description = "Passing free-type-arguments to next screen example",
                    destination = APRFreeArgs
                ),
                AppFeature(
                    name = "NavResult",
                    description = "Returning result tp previous screen",
                    destination = APRNavResult
                ),
            ),
        ),
        HomeItem(
            "Architecture",
            listOf(
                AppFeature(
                    name = "ViewModel",
                    description = "ViewModel usage demo",
                    destination = ArchViewModel
                ),
                AppFeature(
                    name = "Custom SaveState",
                    description = "Custom save and restore state logic case",
                    destination = ArchCustomSaveState
                ),
            ),
        ),
        HomeItem(
            "Advanced examples",
            listOf(
                AppFeature(
                    name = "Back stack alteration",
                    description = "Editing back stack on the fly example",
                    destination = AdvBackStackAlteration
                ),
                AppFeature(
                    name = "Extensions",
                    description = "Example shows how to use nav-destination extensions (eg: Analytics tracking)",
                    destination = AdvExtensions
                ),
            ),
        ),
        HomeItem(
            "Platform ${Platform.name()}",
            Platform.features()
        ),
        HomeItem(
            "3rd party",
            A3rdParty.features()
        ),
    ).filter {
        it.items.isNotEmpty()
    }

val HomeScreen: NavDestination<Unit> by navDestination(ScreenInfo("Home")) {
    val navController = navController()
    Box(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
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
                    onItemClick = { item -> selectedItem = if (selectedItem == item.title) null else item.title },
                    onDestinationSelect = { dest -> navController.navigate(dest) },
                )
            }
        }
        val themeConfig = LocalThemeConfig.current
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = { themeConfig.isDarkMode = !themeConfig.isDarkMode }
        ) {
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
                    .clickable { onItemClick(item) }
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
                        .clickable { onDestinationSelect(it.destination) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.padding(16.dp).weight(1f)) {
                        Text(text = it.name, style = MaterialTheme.typography.titleSmall)
                        if (it.description.isNotBlank()) {
                            VSpacer(4.dp)
                            Text(
                                text = it.description,
                                modifier = Modifier.alpha(0.75f),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "",
                        modifier = Modifier.alpha(0.5f)
                    )
                }
            }
        }
        // selected group indicator
        Box(Modifier.fillMaxHeight().width(3.dp).background(indicatorColor))
    }
}

private data class HomeItem(
    val title: String,
    val items: List<AppFeature>
)