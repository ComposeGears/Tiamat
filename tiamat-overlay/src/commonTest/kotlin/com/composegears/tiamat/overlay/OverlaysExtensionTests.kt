@file:OptIn(ExperimentalTestApi::class)

package com.composegears.tiamat.overlay

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavDestination
import kotlin.test.Test

class OverlaysExtensionTests {

    companion object {

        private val OverlayScreen1: NavDestination<Unit> by navDestination {
            val overlayNavController = navController()
            Column {
                Text(text = "Overlay 1", modifier = Modifier.testTag("Overlay1"))
                Button(
                    onClick = { overlayNavController.navigate(OverlayScreen2) },
                    modifier = Modifier.testTag("OpenOverlay2"),
                ) {
                    Text("Open overlay 2")
                }
                Button(
                    onClick = overlayNavController::overlayBack,
                    modifier = Modifier.testTag("CloseOverlay1"),
                ) {
                    Text("Close overlay 1")
                }
            }
        }

        private val OverlayScreen2: NavDestination<Unit> by navDestination {
            val overlayNavController = navController()
            Column {
                Text(text = "Overlay 2", modifier = Modifier.testTag("Overlay2"))
                Button(
                    onClick = overlayNavController::overlayBack,
                    modifier = Modifier.testTag("CloseOverlay2"),
                ) {
                    Text("Close overlay 2")
                }
            }
        }

        private val HostScreen: NavDestination<Unit> by navDestination(
            OverlaysExtension(
                destinations = arrayOf(OverlayScreen1, OverlayScreen2)
            )
        ) {
            val overlays = ext<OverlaysExtension>() ?: error("OverlaysExtension is missing")
            val overlayNavController = overlays.overlayNavController()
            Column {
                Text(text = "Host", modifier = Modifier.testTag("Host"))
                Button(
                    onClick = { overlayNavController.navigate(OverlayScreen1) },
                    modifier = Modifier.testTag("OpenOverlay1"),
                ) {
                    Text("Open overlay 1")
                }
            }
        }
    }

    @Test
    fun `OverlaysExtension keeps host content visible while overlay is shown`() = runComposeUiTest {
        setContent {
            val navController = rememberNavController(startDestination = HostScreen)
            Navigation(navController = navController, destinations = arrayOf(HostScreen))
        }

        onNodeWithTag("Host").assertExists()
        onNodeWithTag("OpenOverlay1").performClick()

        onNodeWithTag("Host").assertExists()
        onNodeWithTag("Overlay1").assertExists()
    }

    @Test
    fun `OverlaysExtension is visible when open and gone when closes`() = runComposeUiTest {
        setContent {
            val navController = rememberNavController(startDestination = HostScreen)
            Navigation(navController = navController, destinations = arrayOf(HostScreen))
        }

        onNodeWithTag("OpenOverlay1").performClick()
        onNodeWithTag("OpenOverlay2").performClick()

        onNodeWithTag("Host").assertExists()
        onNodeWithTag("Overlay1").assertExists()
        onNodeWithTag("Overlay2").assertExists()

        onNodeWithTag("CloseOverlay2").performClick()

        onNodeWithTag("Overlay1").assertExists()
        onNodeWithTag("Overlay2").assertDoesNotExist()
    }

    @Test
    fun `overlayBack clears the last overlay without hiding the host content`() = runComposeUiTest {
        setContent {
            val navController = rememberNavController(startDestination = HostScreen)
            Navigation(navController = navController, destinations = arrayOf(HostScreen))
        }

        onNodeWithTag("OpenOverlay1").performClick()
        onNodeWithTag("CloseOverlay1").performClick()

        onNodeWithTag("Host").assertExists()
        onNodeWithTag("Overlay1").assertDoesNotExist()
    }
}
