import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import content.examples.koin.KoinLib

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    KoinLib.start()
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App()
    }
}