package composegears.tiamat.example.content.content.advanced

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavEntry
import composegears.tiamat.example.content.content.advanced.OverlayDestinationExtension.Companion.isOverlay
import composegears.tiamat.example.ui.core.AppButton
import composegears.tiamat.example.ui.core.Screen
import composegears.tiamat.example.ui.core.ScreenInfo

val AdvOverlayDestinations by navDestination(ScreenInfo()) {
    Screen("Overlay Destinations") {
        val navController =
            rememberNavController(
                key = "Overlay Destinations nav controller",
                startDestination = AdvOverlayScreen,
                saveable = true,
            )

        val current by navController.currentNavEntryAsState()
        val backstack by navController.currentBackStackFlow.collectAsStateWithLifecycle()
        val state by navController.currentTransitionFlow.collectAsStateWithLifecycle()

        // remap entries to content + overlay
        val stack = remember(current, backstack) { backstack.toMutableList() + listOfNotNull(current) }
        val content = remember(stack) { stack.lastOrNull { !it.isOverlay() } }
        val overlays = remember(stack) { stack.takeLastWhile { it.isOverlay() } }

        NavigationScene(
            navController = navController,
            destinations = arrayOf(
                AdvOverlayScreen,
                AdvOverlayBottomSheet,
                AdvOverlayDialog,
            ),
        ) {
            // animate main content
            AnimatedContent(
                targetState = content,
                contentKey = { it?.contentKey() },
                transitionSpec = { navigationSlideInOut(state?.isForward ?: true) },
            ) {
                CompositionLocalProvider(
                    LocalNavAnimatedVisibilityScope provides this,
                ) {
                    EntryContent(it)
                }
            }
            // draw overlays on top of content
            Box {
                for (entry in overlays) {
                    EntryContent(entry)
                }
            }
        }
    }
}

class OverlayDestinationExtension<T : Any> : NavExtension<T> {
    companion object {
        fun NavEntry<*>.isOverlay(): Boolean =
            destination.ext<OverlayDestinationExtension<*>>() != null
    }
}

private val AdvOverlayScreen by navDestination<Unit> {
    val navController = navController()

    val current by navController.currentNavEntryAsState()
    val backstack by navController.currentBackStackFlow.collectAsStateWithLifecycle()
    val canGoBack = remember { backstack.isNotEmpty() }
    val stack = remember(current, backstack) { backstack.toMutableList() + listOfNotNull(current) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { AdvOverlayContentButtons(nc = navController) }
        if (canGoBack) {
            item {
                AppButton(
                    "Back",
                    endIcon = Icons.Default.Close,
                    onClick = { navController.back() }
                )
            }
        }
        item { Text("Stack") }
        items(stack) { entry ->
            Text(entry.destination.name)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private val AdvOverlayBottomSheet by navDestination<Unit>(OverlayDestinationExtension()) {
    val navController = navController()
    ModalBottomSheet(onDismissRequest = navController::back) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AdvOverlayContentButtons(nc = navController)
            AppButton(
                "Close",
                endIcon = Icons.Default.Close,
                onClick = { navController.back() }
            )
        }
    }
}

private val AdvOverlayDialog by navDestination<Unit>(OverlayDestinationExtension()) {
    val navController = navController()
    AlertDialog(
        onDismissRequest = { navController.back() },
        text = { Text("Dialog") },
        confirmButton = {
            AppButton(
                "Open Screen",
                onClick = { navController.navigate(AdvOverlayScreen) }
            )
        },
        dismissButton = {
            AppButton(
                "Back",
                onClick = { navController.back() }
            )
        },
    )
}

@Composable
private fun AdvOverlayContentButtons(
    nc: NavController,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppButton(
            "Open Bottom sheet",
            endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
            onClick = { nc.navigate(AdvOverlayBottomSheet) }
        )
        AppButton(
            "Open Dialog",
            endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
            onClick = { nc.navigate(AdvOverlayDialog) }
        )
        AppButton(
            "Open Screen",
            endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
            onClick = { nc.navigate(AdvOverlayScreen) }
        )
    }
}