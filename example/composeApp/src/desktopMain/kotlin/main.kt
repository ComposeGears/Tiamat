import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.*
import com.composegears.tiamat.LocalNavBackHandler

fun main() = application {
    val backHandler = LocalNavBackHandler.current
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            placement = WindowPlacement.Floating,
            position = WindowPosition.Aligned(Alignment.Center)
        ),
        onKeyEvent = {
           it.key == Key.Escape && it.type== KeyEventType.KeyUp && backHandler.back()
        },
        title = "ComposeGears-Nav-Example"
    ) {
        App()
    }
}

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}