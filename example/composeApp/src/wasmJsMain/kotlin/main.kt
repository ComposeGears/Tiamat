import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.CanvasBasedWindow
import com.composegears.tiamat.LocalNavBackHandler
import content.examples.koin.KoinLib
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    KoinLib.start()
    val focusRequester = FocusRequester()

    CanvasBasedWindow(canvasElementId = "TiamatTarget") {
        PageLoadNotify()

        // workaround to make window always focused
        LaunchedEffect(Unit) {
            while (true) {
                delay(100)
                focusRequester.requestFocus()
            }
        }
        val backHandler = LocalNavBackHandler.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusTarget()
                .onKeyEvent {
                    it.key == Key.Escape && it.type == KeyEventType.KeyUp && backHandler.back()
                }
        ) {
            App()
            Text(
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                text = "Pre Alpha (Wasm)",
            )
        }
    }
}