package composegears.tiamat.sample.content

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.TiamatPreview
import com.composegears.tiamat.compose.navController
import com.composegears.tiamat.compose.navDestination
import com.composegears.tiamat.compose.navigate
import com.composegears.tiamat.navigation.NavDestination
import composegears.tiamat.sample.content.layouts.LayoutAdaptiveListDetails
import composegears.tiamat.sample.content.layouts.LayoutOverlayDestinations
import composegears.tiamat.sample.content.layouts.LayoutTwoPane
import composegears.tiamat.sample.content.navigation.actions.NavActionForwardAndBack
import composegears.tiamat.sample.content.navigation.actions.NavActionReplace
import composegears.tiamat.sample.content.navigation.actions.NavActionRoute
import composegears.tiamat.sample.content.navigation.animations.NavAnimationCustom
import composegears.tiamat.sample.content.navigation.animations.NavAnimationSharedElementTransition
import composegears.tiamat.sample.content.navigation.data.NavDataArgs
import composegears.tiamat.sample.content.navigation.data.NavDataFreeArgs
import composegears.tiamat.sample.content.navigation.data.NavDataResult
import composegears.tiamat.sample.content.navigation.data.NavDataSerializable
import composegears.tiamat.sample.content.navigation.patterns.NavPatternNested
import composegears.tiamat.sample.content.navigation.patterns.NavPatternTabs
import composegears.tiamat.sample.content.other.OtherDestinationsGraph
import composegears.tiamat.sample.content.other.OtherExtensions
import composegears.tiamat.sample.content.other.OtherNavStackAlteration
import composegears.tiamat.sample.content.state.StateCustomSaveState
import composegears.tiamat.sample.content.state.StateRetain
import composegears.tiamat.sample.content.state.StateViewModel
import composegears.tiamat.sample.icons.DarkMode
import composegears.tiamat.sample.icons.Icons
import composegears.tiamat.sample.icons.KeyboardArrowRight
import composegears.tiamat.sample.icons.KeyboardArrowUp
import composegears.tiamat.sample.platform.Platform
import composegears.tiamat.sample.platform.features
import composegears.tiamat.sample.platform.name
import composegears.tiamat.sample.ui.*

internal val HomeItems =
    listOf(
        HomeItem(
            "Getting Started",
            listOf(
                AppFeature(
                    name = "Forward & back",
                    description = "Simple navigation back & forward case",
                    destination = NavActionForwardAndBack
                ),
                AppFeature(
                    name = "Replace",
                    description = "Replace (navigate without adding current destination to back stack) case",
                    destination = NavActionReplace
                ),
                AppFeature(
                    name = "NavArgs",
                    description = "Passing navigation-arguments to next screen",
                    destination = NavDataArgs
                ),
                AppFeature(
                    name = "NavResult",
                    description = "Returning result to previous screen",
                    destination = NavDataResult
                ),
            ),
        ),
        HomeItem(
            "Advanced Data Passing",
            listOf(
                AppFeature(
                    name = "FreeArgs",
                    description = "Passing free-type-arguments to next screen example",
                    destination = NavDataFreeArgs
                ),
                AppFeature(
                    name = "Serializable Data & Arguments",
                    description = "Passing @Serializable data as navArgs / freeArgs / navResult",
                    destination = NavDataSerializable
                ),
            ),
        ),
        HomeItem(
            "Navigation & Patterns",
            listOf(
                AppFeature(
                    name = "Routing (experimental)",
                    description = "Advanced Route-Api demo (building nav-path)",
                    destination = NavActionRoute
                ),
                AppFeature(
                    name = "Tabs navigation",
                    description = "Simple tab's navigation with a separate nav controllers for each tab. " +
                        "Sample use `popToTop` nav action to bring to from previously opened screen",
                    destination = NavPatternTabs
                ),
                AppFeature(
                    name = "Nested navigation",
                    description = "Multiple nested nav controllers case",
                    destination = NavPatternNested
                ),
            ),
        ),
        HomeItem(
            "Animations",
            listOf(
                AppFeature(
                    name = "Custom animation",
                    description = "On fly customizable navigation animation",
                    destination = NavAnimationCustom
                ),
                AppFeature(
                    name = "Shared element transition",
                    description = "Example shows how to use shared element transition",
                    destination = NavAnimationSharedElementTransition
                ),
            ),
        ),
        HomeItem(
            "Data & State Management",
            listOf(
                AppFeature(
                    name = "ViewModel",
                    description = "ViewModel usage demo",
                    destination = StateViewModel
                ),
                AppFeature(
                    name = "Custom SaveState",
                    description = "Custom save and restore state logic case",
                    destination = StateCustomSaveState
                ),
                AppFeature(
                    name = "Retain API",
                    description = "Example shows how to use new Compose retain API inside Tiamat destination",
                    destination = StateRetain
                ),
            ),
        ),
        HomeItem(
            "Layouts",
            listOf(
                AppFeature(
                    name = "Overlay Destinations",
                    description = "Example shows how to navigate from and to overlays (dialogs, bottom sheets, etc)",
                    destination = LayoutOverlayDestinations
                ),
                AppFeature(
                    name = "Two pane",
                    description = "Example shows how to display two pane UI (list + details)",
                    destination = LayoutTwoPane
                ),
                AppFeature(
                    name = "Adaptive list + details",
                    description = "Example shows how to display list + details UI based on screen size",
                    destination = LayoutAdaptiveListDetails
                ),
            ),
        ),
        HomeItem(
            "Other Features",
            listOf(
                AppFeature(
                    name = "Nav stack alteration",
                    description = "Editing nav stack on the fly example",
                    destination = OtherNavStackAlteration
                ),
                AppFeature(
                    name = "Extensions",
                    description = "Example shows how to use nav-destination extensions (eg: Analytics tracking)",
                    destination = OtherExtensions
                ),
                AppFeature(
                    name = "Auto destinations graph",
                    description = "Example shows how to use `InstallIn` annotation and generate destinations graph",
                    destination = OtherDestinationsGraph
                ),
            ),
        ),
        HomeItem(
            "Platform ${Platform.name()}",
            Platform.features()
        ),
    ).filter {
        it.items.isNotEmpty()
    }

val HomeScreen: NavDestination<Unit> by navDestination(ScreenInfo("Home")) {
    val navController = navController()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .systemBarsPadding(),
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
            Icon(Icons.DarkMode, "")
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
                Icon(Icons.KeyboardArrowUp, "", Modifier.rotate(iconRotation))
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
                        imageVector = Icons.KeyboardArrowRight,
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

internal data class HomeItem(
    val title: String,
    val items: List<AppFeature>
)

@Preview
@Composable
private fun HomeScreenPreview() = AppTheme {
    TiamatPreview(destination = HomeScreen)
}