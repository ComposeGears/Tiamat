@file:Suppress("Filename", "MissingPackageDeclaration")

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.composegears.tiamat.LocalNavBackHandler
import content.examples.koin.KoinLib

fun main() {
    KoinLib.start()
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
            App()
        }
    }
}

@Preview
@Composable
private fun AppDesktopPreview() {
    App()
}