@file:Suppress("Filename")

package composegears.tiamat.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import com.composegears.tiamat.*
import composegears.tiamat.example.content.App
import composegears.tiamat.example.extra.A3rdParty
import composegears.tiamat.example.platform.Platform
import composegears.tiamat.example.platform.start
import composegears.tiamat.example.ui.core.LocalScreenHandler
import composegears.tiamat.example.ui.core.ScreenInfo
import kotlinx.browser.window
import org.w3c.dom.PopStateEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

const val TITLE = "Tiamat Wasm"
const val HOME = "Home"
val REDIRECT_DELAY = 100.milliseconds

var redirectTimeout = now()

external fun onLoadFinished()

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    Platform.start()
    A3rdParty.start()
    ComposeViewport(viewportContainerId = "TiamatTarget") {
        val screenHandler = LocalScreenHandler.current
        DisposableEffect(Unit) {
            onLoadFinished()
            // update current url + add to history
            val listener = screenHandler.addScreenOpenListener { nc, _ ->
                appendState(nc)
            }
            onDispose {
                screenHandler.removeScreenOpenListener(listener)
            }
        }
        App(
            navControllerConfig = {
                // open link from browser upon initialization
                loadFromURL(this)
                // listen to browser back/forward navigation
                window.addEventListener("popstate") { e ->
                    loadFromState(this, (e as? PopStateEvent?)?.state)
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
                        text = "Wasm Alpha",
                    )
                }
            }
        )
    }
}

// Simple web browser history & url impl

// ---------------- web operations -------------------------------

fun loadFromURL(navController: NavController) {
    redirectTimeout = now()
    val browserPath = currentPath()
    val browserRoute = path2route(browserPath)
    if (browserPath.isBlank() || browserPath == "/") {
        window.history.replaceState("$HOME/".toJsString(), "", "./#$HOME")
        setTitle(HOME)
    } else {
        window.history.replaceState(browserRoute.toJsString(), "", "./#$HOME")
        navController.setRoute(browserRoute)
    }
}

fun loadFromState(navController: NavController, state: Any?) {
    redirectTimeout = now()
    (state as? JsString?)?.toString()?.let { navController.setRoute(it) } ?: loadFromURL(navController)
}

fun appendState(navController: NavController) {
    val browserPath = currentPath()
    val navControllerRoute = navController.getRoute()
    val navControllerPath = route2path(navControllerRoute)
    if (browserPath != navControllerPath) {
        if (redirectTimeout.elapsedNow() < REDIRECT_DELAY) {
            window.history.replaceState(navControllerRoute.toJsString(), "", "./#$navControllerPath")
        } else {
            redirectTimeout = now()
            window.history.pushState(navControllerRoute.toJsString(), "", "./#$navControllerPath")
        }
    }
    setTitle(navController.current?.name ?: TITLE)
}

// --------------- helpers --------------------------------------

fun currentPath() = window.location.href
    .replace(window.location.origin, "")
    .replace("/#", "")

fun setTitle(title: String) {
    window.document.title = title
}

fun now() = TimeSource.Monotonic.markNow()

fun path2route(path: String): String {
    // here we parse path and make nav-route from it
    // append HOME dest as first one for any path (if needed)
    return when {
        path == "/" -> HOME
        path.startsWith(HOME) -> path
        else -> "$HOME/$path"
    }
}

fun route2path(route: String): String {
    // here we convert route to web path (we can clip or make path shorter)
    // clip HOME dest if there is any other screen is opened as HOME is always the 1st by default
    return when {
        route.startsWith("$HOME/") -> route.substringAfter("$HOME/")
        else -> route
    }
}

fun <Args> NavDestination<Args>.parseEntry(argStr: String?): NavEntry<Args> {
    val ext = ext<ScreenInfo<Args>>()
    val args = ext?.stringToArgs?.invoke(argStr)
    return toNavEntry(navArgs = args)
}

@OptIn(TiamatExperimentalApi::class)
fun NavController.setRoute(path: String) {
    route(
        Route.build(forceReplace = true) {
            path.split("/").forEach { segment ->
                val targetName = segment.substringBefore("?")
                val argsStr = segment.substringAfter("?", "").takeIf { it.isNotBlank() }
                route(
                    description = "Follow: $segment",
                    entryProvider = { nc ->
                        val destination = nc.findDestination {
                            val ext = it.ext<ScreenInfo<*>>() ?: return@findDestination false
                            val name = ext.name ?: it.name
                            name == targetName
                        } ?: return@route null
                        destination.parseEntry(argsStr)
                    }
                )
            }
        }
    )
}

private fun <Args> NavEntry<Args>.toPath(): String? {
    val ext = destination.ext<ScreenInfo<Args>>() ?: return null
    val name = ext.name ?: destination.name
    val args = navArgs?.let(ext.argsToString)
    return if (args == null) name else "$name?$args"
}

fun NavController.getRoute(): String {
    val segments = mutableListOf<String>()
    var nc: NavController? = this
    while (nc != null) {
        nc.currentNavEntry?.toPath()?.let { segments.add(0, it) }
        nc = nc.parent
    }
    return segments.joinToString("/")
}