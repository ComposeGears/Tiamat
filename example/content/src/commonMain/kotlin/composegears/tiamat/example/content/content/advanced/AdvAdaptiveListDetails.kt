package composegears.tiamat.example.content.content.advanced

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import composegears.tiamat.example.ui.core.AppButton
import composegears.tiamat.example.ui.core.HSpacer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
val AdvAdaptiveListDetails by navDestination<Unit> {
    BoxWithConstraints {
        val sizeClass by remember(maxWidth) {
            mutableStateOf(
                WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight)).widthSizeClass
            )
        }
        val menuItems = remember {
            listOf(
                MenuItem(Icons.Filled.Home, "Home"),
                MenuItem(Icons.Filled.Tab, "Tab"),
                MenuItem(Icons.Filled.Web, "Web"),
            )
        }
        var activeMenuItem by remember { mutableStateOf(menuItems.first()) }
        val nc = rememberNavController(key = "a", startDestination = AdvAdaptiveListDetailsList)
        Surface {
            Column {
                // screen content - toolbar
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Adaptive List Details", style = MaterialTheme.typography.titleLarge)
                            Text("$sizeClass", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    navigationIcon = {
                        val parentNavController = navController()
                        IconButton(onClick = parentNavController::back) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                // screen body content - making it `movableContentOf` as it will be moved in between row and column
                val content = remember {
                    movableContentOf { sizeClass: WindowWidthSizeClass ->
                        NavigationScene(
                            navController = nc,
                            destinations = arrayOf(
                                AdvAdaptiveListDetailsList,
                                AdvAdaptiveListDetailsDetails1,
                                AdvAdaptiveListDetailsDetails2
                            ),
                        ) {
                            val current by nc.currentNavEntryAsState()
                            // look at size class
                            if (sizeClass == WindowWidthSizeClass.Compact) {
                                // as for small screen -> render in 1 pane
                                AnimatedContent(
                                    targetState = current,
                                    transitionSpec = { navigationFadeInOut() },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    EntryContent(it)
                                }
                            } else Row(Modifier.fillMaxSize()) {
                                // as for Medium & Expanded - draw in 2 panes
                                val backstack by nc.currentBackStackFlow.collectAsState()
                                val listEntry = remember(current) { backstack.firstOrNull() ?: current }
                                val itemEntry = remember(current) { current?.takeIf { backstack.isNotEmpty() } }
                                // draw list
                                Box(Modifier.width(300.dp).fillMaxHeight()) {
                                    EntryContent(listEntry)
                                }
                                // draw last entry (unless it is list)
                                AnimatedContent(
                                    targetState = itemEntry,
                                    contentKey = { it?.contentKey() },
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    transitionSpec = { navigationFadeInOut() }
                                ) {
                                    if (it != null) EntryContent(it)
                                    else Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("No item selected")
                                    }
                                }
                            }
                        }
                    }
                }
                // look at size class and draw  content depend on it
                if (sizeClass == WindowWidthSizeClass.Compact) Column {
                    Box(Modifier.weight(1f)) {
                        content(sizeClass)
                    }
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        menuItems.forEach { item ->
                            NavigationBarItem(
                                selected = item == activeMenuItem,
                                onClick = { activeMenuItem = item },
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) }
                            )
                        }
                    }
                } else Row {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        menuItems.forEach { item ->
                            NavigationRailItem(
                                selected = item == activeMenuItem,
                                onClick = { activeMenuItem = item },
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) }
                            )
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        content(sizeClass)
                    }
                }
            }
        }
    }
}

private val AdvAdaptiveListDetailsList by navDestination<Unit> {
    val nc = navController()
    val items = remember {
        (0..10).map { "Item $it" }
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(items) { item ->
                AppButton(
                    item,
                    modifier = Modifier.widthIn(min = 200.dp),
                    endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    onClick = { nc.navigate(AdvAdaptiveListDetailsDetails1, item) }
                )
            }
        }
    }
}

private val AdvAdaptiveListDetailsDetails1 by navDestination<String> {
    val nc = navController()
    val args = navArgs()
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            Text("Selected item: ${navArgs()}", style = MaterialTheme.typography.bodyMedium)
            Row {
                AppButton(
                    "Back",
                    startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    onClick = { nc.back() }
                )
                HSpacer()
                AppButton(
                    "Next",
                    endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    onClick = { nc.navigate(AdvAdaptiveListDetailsDetails2, args) }
                )
            }
        }
    }
}

private val AdvAdaptiveListDetailsDetails2 by navDestination<String> {
    val nc = navController()
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 3", style = MaterialTheme.typography.headlineMedium)
            Text("Selected item: ${navArgs()}", style = MaterialTheme.typography.bodyMedium)
            AppButton(
                "Back",
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

private data class MenuItem(val icon: ImageVector, val title: String)