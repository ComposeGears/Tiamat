@file:Suppress("Filename")

package composegears.tiamat.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import com.composegears.tiamat.NavController
import composegears.tiamat.example.content.App
import composegears.tiamat.example.extra.A3rdParty
import composegears.tiamat.example.platform.Platform
import composegears.tiamat.example.platform.getDestinationPath
import composegears.tiamat.example.platform.openDestinationPath
import kotlinx.browser.window
import org.w3c.dom.PopStateEvent
import kotlin.time.TimeSource

const val TITLE = "Tiamat Wasm"
const val HOME_SCREEN = "HomeScreen"

external fun onLoadFinished()

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    Platform.start()
    A3rdParty.start()
    ComposeViewport(viewportContainerId = "TiamatTarget") {
        DisposableEffect(Unit) {
            onLoadFinished()
            onDispose { }
        }
        Box {
            App(navControllerConfig = {
                // open link from browser upon initialization
                initializeFromBrowserPath(this)
                // listen to browser back/forward navigation
                window.addEventListener("popstate") { e ->
                    invalidateState(this, (e as? PopStateEvent?)?.state)
                }
                // listen to navController navigation and update browser history
                addOnDestinationChangedListener { nc, _ ->
                    invalidateNavControllerPath(nc)
                }
            })
            Text(
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                text = "Wasm Alpha",
            )
        }
    }
}

fun currentPath() = window.location.href
    .replace(window.location.origin, "")
    .replace("/#", "")

fun now() = TimeSource.Monotonic.markNow()

fun setTitles(title: String) {
    window.document.title = title
}

fun initializeFromBrowserPath(navController: NavController) {
    val browserPath = currentPath()
    if (browserPath.isBlank() || browserPath == "/") {
        window.history.replaceState(null, "", "./#$HOME_SCREEN")
        setTitles(HOME_SCREEN)
    }
    navController.openDestinationPath(browserPath.fullPath())
}

fun invalidateState(navController: NavController, state: Any?) {
    // todo utilize state
    initializeFromBrowserPath(navController)
}

fun invalidateNavControllerPath(navController: NavController) {
    val browserPath = currentPath()
    val navControllerPath = navController.getDestinationPath().shortPath()
    if (browserPath != navControllerPath) {
        window.history.pushState(null, "", "./#$navControllerPath")
    }
    setTitles(navController.current?.name ?: TITLE)
}

// clip ths `HomeScreen` in case we are at some other screen, as home screen is always the 1st one
fun String.shortPath(): String =
    if (startsWith("$HOME_SCREEN/")) {
        substringAfter("$HOME_SCREEN/")
    } else this

// append `HomeScreen` in case we are at some other screen, so it will be in backstack
fun String.fullPath(): String =
    if (this == "/" || this == HOME_SCREEN) HOME_SCREEN
    else "$HOME_SCREEN/$this"