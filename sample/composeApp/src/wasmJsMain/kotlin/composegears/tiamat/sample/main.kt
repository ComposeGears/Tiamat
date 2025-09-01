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
                // open link from browser upon initialization
                // loadFromURL(this)
                // listen to browser back/forward navigation
                // window.addEventListener("popstate") { e ->
                //     loadFromState(this, (e as? PopStateEvent?)?.state)
                // }
                // handle navigation events
                // setOnNavigationListener { from, to, isForward ->
                //     if (isForward) {
                //         appendState(this)
                //     }
                // }
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
                        text = "Wasm Alpha",
                    )
                }
            }
        )
    }
}

// todo add browser history & state save/restore logic

// Simple web browser history & url impl

// ---------------- web operations -------------------------------

/*
const val HOME = "Home"
val REDIRECT_DELAY = 100.milliseconds

var redirectTimeout = now()

fun now() = TimeSource.Monotonic.markNow()

fun setTitle(title: String) {
    window.document.title = title
}

fun getCurrentPath() = window.location.href
    .replace(window.location.origin, "")
    .replace("/#", "")

fun loadFromURL(navController: NavController) {
    redirectTimeout = now()
    val browserPath = getCurrentPath()
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
    val browserPath = getCurrentPath()
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
    setTitle(navController.getCurrentNavEntry()?.destination?.name ?: TITLE)
}

// --------------- helpers --------------------------------------

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
    route {
        path.split("/").forEach { segment ->
            val targetName = segment.substringBefore("?")
            val argsStr = segment.substringAfter("?", "").takeIf { it.isNotBlank() }
            if (targetName == "nc") navController(argsStr)
            else destination(targetName)
        }
    }
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
        nc.getCurrentNavEntry()?.toPath()?.let { segments.add(0, it) }
        segments.add(0, "nc?${nc.key}")
        nc = nc.parent
    }
    return segments.joinToString("/")
}*/
