@file:Suppress("Filename")

package composegears.tiamat.example

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.composegears.tiamat.LocalNavBackHandler
import composegears.tiamat.example.content.App
import composegears.tiamat.example.extra.A3rdParty
import composegears.tiamat.example.platform.Platform
import composegears.tiamat.example.platform.start

@OptIn(ExperimentalFoundationApi::class)
fun main() {
    Platform.start()
    A3rdParty.start()
    application {
        val backHandler = LocalNavBackHandler.current
        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(
                placement = WindowPlacement.Floating,
                position = WindowPosition.Aligned(Alignment.Center),
                size = DpSize(1200.dp, 800.dp)
            ),
            onKeyEvent = {
                it.key == Key.Escape && it.type == KeyEventType.KeyUp && backHandler.back()
            },
            title = "Tiamat Nav-Example"
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .onClick(
                        enabled = true,
                        matcher = PointerMatcher.mouse(PointerButton.Back),
                        onClick = { backHandler.back() }
                    )
            ) {
                App()
            }
        }
    }
}

@Preview
@Composable
private fun AppDesktopPreview() {
    App()
}