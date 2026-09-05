package composegears.tiamat.sample.content.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavDestination
import com.composegears.tiamat.overlay.OverlaysExtension
import com.composegears.tiamat.overlay.overlayBack
import composegears.tiamat.sample.icons.Close
import composegears.tiamat.sample.icons.Icons
import composegears.tiamat.sample.icons.KeyboardArrowLeft
import composegears.tiamat.sample.icons.KeyboardArrowRight
import composegears.tiamat.sample.ui.AppButton
import composegears.tiamat.sample.ui.AppTheme
import composegears.tiamat.sample.ui.Screen
import composegears.tiamat.sample.ui.ScreenInfo

val LayoutOverlayDestinations by navDestination(ScreenInfo()) {
    Screen("Overlay Destinations") {
        val navController =
            rememberNavController(
                key = "Overlay Destinations nav controller",
                startDestination = LayoutOverlayScreen,
            )

        Navigation(
            navController = navController,
            destinations = arrayOf(
                LayoutOverlayScreen,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private val LayoutOverlayBottomSheet: NavDestination<Unit> by navDestination {
    val overlayNavController = navController()
    val rootNavController = overlayNavController.parent ?: error("Root NavController is missing")

    ModalBottomSheet(onDismissRequest = overlayNavController::overlayBack) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LayoutOverlayContentButtons(
                rootNavController = rootNavController,
                overlayNavController = overlayNavController,
            )
            AppButton(
                "Close",
                endIcon = Icons.Close,
                onClick = overlayNavController::overlayBack,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private val LayoutOverlayDialog: NavDestination<Unit> by navDestination {
    val overlayNavController = navController()
    val rootNavController = overlayNavController.parent ?: error("Root NavController is missing")

    BasicAlertDialog(
        onDismissRequest = overlayNavController::overlayBack,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LayoutOverlayContentButtons(
                    rootNavController = rootNavController,
                    overlayNavController = overlayNavController,
                )
                AppButton(
                    "Close",
                    endIcon = Icons.Close,
                    onClick = overlayNavController::overlayBack,
                )
            }
        }
    )
}

@Composable
private fun LayoutOverlayContentButtons(
    rootNavController: NavController,
    overlayNavController: NavController,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppButton(
            "Open New Dialog",
            endIcon = Icons.KeyboardArrowRight,
            onClick = { overlayNavController.navigate(LayoutOverlayDialog) }
        )
        AppButton(
            "Open New Bottom Sheet",
            endIcon = Icons.KeyboardArrowRight,
            onClick = { overlayNavController.navigate(LayoutOverlayBottomSheet) }
        )
        AppButton(
            "Open Screen",
            endIcon = Icons.KeyboardArrowRight,
            onClick = { rootNavController.navigate(LayoutOverlayScreen) }
        )
    }
}

private val LayoutOverlayScreen: NavDestination<Unit> by navDestination(
    OverlaysExtension(
        arrayOf(
            LayoutOverlayBottomSheet,
            LayoutOverlayDialog,
        )
    )
) {
    val navController = navController()
    val overlayNavController = ext<OverlaysExtension>()?.overlayNavController() ?: error("OverlaysExtension is missing")

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp).align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppButton(
                text = "Open overlay dialog",
                endIcon = Icons.KeyboardArrowRight,
                onClick = { overlayNavController.navigate(LayoutOverlayDialog) }
            )
            AppButton(
                text = "Open overlay Bottom Sheet",
                endIcon = Icons.KeyboardArrowRight,
                onClick = { overlayNavController.navigate(LayoutOverlayBottomSheet) }
            )
            AppButton(
                text = "Open new screen",
                endIcon = Icons.KeyboardArrowRight,
                onClick = { navController.navigate(LayoutOverlayScreen) }
            )
            AppButton(
                text = "Back",
                enabled = navController.canNavigateBack(),
                startIcon = Icons.KeyboardArrowLeft,
                onClick = navController::back
            )
        }
    }
}

@Preview
@Composable
private fun LayoutOverlayDestinationsPreview() = AppTheme {
    TiamatPreview(destination = LayoutOverlayDestinations)
}
