import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.CanvasBasedWindow
import content.examples.koin.KoinLib

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    KoinLib.start()
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        Box {
            App()
            Text(
                modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                text = "Pre Alpha (WASM)",
            )
        }
    }
}