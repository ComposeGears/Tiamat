@file:Suppress("Filename")

package composegears.tiamat.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.window

const val TITLE = "Tiamat Wasm"

external fun onLoadFinished()

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "TiamatTarget") {
        LaunchedEffect(Unit) {
            onLoadFinished()
        }
        App(
            navControllerConfig = {
                setOnNavigationListener { from, to, isForward ->
                    window.document.title = TITLE + " / " + (to?.destination?.name ?: "")
                }
            },
            overlay = {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        text = "Wasm Beta",
                    )
                }
            }
        )
    }
}