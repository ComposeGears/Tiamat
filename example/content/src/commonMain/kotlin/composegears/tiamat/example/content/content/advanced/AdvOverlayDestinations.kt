package composegears.tiamat.example.content.content.advanced

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavEntry
import composegears.tiamat.example.ui.core.AppButton
import composegears.tiamat.example.ui.core.Screen
import composegears.tiamat.example.ui.core.ScreenInfo

data class SceneGroup(
    val root: NavEntry<*>?,
    val overlays: List<NavEntry<*>>,
)

class OverlayDestinationExtension<T : Any> : NavExtension<T>

val AdvOverlayDestinations by navDestination(ScreenInfo()) {
    Screen("Overlay Destinations") {
        val navController =
            rememberNavController(
                key = "Overlay Destinations nav controller",
                startDestination = Screen,
                saveable = true,
            )

        val current by navController.currentNavEntryAsState()
        val backstack by navController.currentBackStackFlow.collectAsStateWithLifecycle()
        val state by navController.currentTransitionFlow.collectAsStateWithLifecycle()

        val sceneGroup: SceneGroup = remember(current, backstack) {
            var sceneRoot = current
            val overlays: List<NavEntry<*>> = buildList {
                if (sceneRoot?.destination?.ext<OverlayDestinationExtension<*>>() != null) {
                    add(sceneRoot)
                    for (entry in backstack.reversed()) {
                        sceneRoot = entry
                        if (entry.destination.ext<OverlayDestinationExtension<*>>() != null) {
                            add(entry)
                        } else {
                            break
                        }
                    }
                }
            }.reversed()

            SceneGroup(
                root = sceneRoot,
                overlays = overlays,
            )
        }

        NavigationScene(
            navController = navController,
            destinations = arrayOf(
                Screen,
                OverlayBottomSheet,
                OverlayDialog,
            ),
        ) {
            AnimatedContent(
                targetState = sceneGroup.root,
                contentKey = { it?.contentKey() },
                transitionSpec = {
                    navigationPlatformDefault(state?.isForward ?: true)
                },
            ) {
                CompositionLocalProvider(
                    LocalNavAnimatedVisibilityScope provides this,
                ) {
                    EntryContent(it)
                }
            }

            Box {
                for (entry in sceneGroup.overlays) {
                    if (entry != sceneGroup.root) {
                        EntryContent(entry)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private val Screen by navDestination<Unit> {
    val navController = navController()

    val current by navController.currentNavEntryAsState()
    val backstack by navController.currentBackStackFlow.collectAsStateWithLifecycle()
    val entries = remember(current, backstack) {
        (listOf(current) + backstack).filterNotNull()
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Buttons(nc = navController)
        }

        item {
            Text("Backstack")
        }

        items(entries) { entry ->
            ListItem(
                headlineContent = {
                    Text(entry.destination.name)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private val OverlayBottomSheet by navDestination<Unit>(
    OverlayDestinationExtension(),
) {
    val navController = navController()

    ModalBottomSheet(
        onDismissRequest = navController::back,
    ) {
        Buttons(nc = navController)

        AppButton(
            "Close",
            endIcon = Icons.Default.Close,
            onClick = { navController.back() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private val OverlayDialog by navDestination<Unit>(
    OverlayDestinationExtension(),
) {
    val navController = navController()

    AlertDialog(
        onDismissRequest = {
            navController.back()
        },
        text = {
            Text("Dialog")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    navController.navigate(Screen)
                },
            ) {
                Text("To Screen")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    navController.back()
                },
            ) {
                Text("Back")
            }
        },
    )
}

@Composable
private fun Buttons(
    modifier: Modifier = Modifier,
    nc: NavController,
) {
    Column(
        modifier = modifier.padding(16.dp),
    ) {
        AppButton(
            "To Bottom sheet",
            endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
            onClick = { nc.navigate(OverlayBottomSheet) }
        )

        AppButton(
            "To Dialog",
            endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
            onClick = { nc.navigate(OverlayDialog) }
        )

        AppButton(
            "To Screen",
            endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
            onClick = { nc.navigate(Screen) }
        )
    }
}
