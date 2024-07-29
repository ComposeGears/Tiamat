@file:Suppress("Filename")

package composegears.tiamat.example

import PageLoadNotify
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.CanvasBasedWindow
import composegears.tiamat.sample.koin.KoinLib

external fun onLoadFinished()

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    KoinLib.start()
    CanvasBasedWindow(canvasElementId = "TiamatTarget") {
        PageLoadNotify()
        Box {
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