@file:Suppress("Filename")
@file:OptIn(ExperimentalComposeUiApi::class)

package composegears.tiamat.example

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import composegears.tiamat.example.content.App
import composegears.tiamat.example.extra.A3rdParty
import composegears.tiamat.example.platform.Platform
import composegears.tiamat.example.platform.start

@OptIn(ExperimentalFoundationApi::class)
fun main() {
    Platform.start()
    A3rdParty.start()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(
                placement = WindowPlacement.Floating,
                position = WindowPosition.Aligned(Alignment.Center),
                size = DpSize(1200.dp, 1000.dp)
            ),
            title = "Tiamat Nav-Example"
        ) {
            App()
        }
    }
}

@Preview
@Composable
private fun AppDesktopPreview() {
    App()
}