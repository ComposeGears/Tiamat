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
import com.composegears.tiamat.sample.BuildConfig

external fun onLoadFinished()

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "TiamatTarget") {
        LaunchedEffect(Unit) { onLoadFinished() }
        App(
            navControllerConfig = { Browser.bind(this) },
            overlay = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        text = "Work in progress...",
                    )
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        text = "Tiamat WASM ${BuildConfig.TIAMAT_VERSION}",
                    )
                }
            }
        )
    }
}