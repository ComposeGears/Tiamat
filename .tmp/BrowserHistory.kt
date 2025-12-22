package composegears.tiamat.sample

import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavDestination
import com.composegears.tiamat.navigation.NavEntry
import com.composegears.tiamat.navigation.route
import composegears.tiamat.sample.ui.ScreenInfo
import kotlin.js.JsName
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

private const val HOME = "Home"
private val redirectDelay = 100.milliseconds
private var redirectMark: ValueTimeMark = TimeSource.Monotonic.markNow()

fun loadFromURL(navController: NavController) {
    redirectMark = TimeSource.Monotonic.markNow()
    val browserPath = getCurrentPath()
    val browserRoute = path2route(browserPath)
    if (browserPath.isBlank() || browserPath == "/") {
        history.replaceState(heroRoute().toJsString(), "", "./#$HOME")
        setTitle(HOME)
    } else {
        history.replaceState(browserRoute.toJsString(), "", "./#$HOME")
        navController.setRoute(browserRoute)
    }
}

fun loadFromState(navController: NavController, state: Any?) {
    redirectMark = TimeSource.Monotonic.markNow()
    (state as? JsString?)?.toString()?.let { navController.setRoute(it) } ?: loadFromURL(navController)
}

fun appendState(navController: NavController) {
    val browserPath = getCurrentPath()
    val navRoute = navController.getRoute()
    val navPath = route2path(navRoute)
    if (browserPath != navPath) {
        if (redirectMark.elapsedNow() < redirectDelay) {
            history.replaceState(navRoute.toJsString(), "", "./#$navPath")
        } else {
            redirectMark = TimeSource.Monotonic.markNow()
            history.pushState(navRoute.toJsString(), "", "./#$navPath")
        }
    }
    setTitle(navController.getCurrentNavEntry()?.destination?.name ?: TITLE)
}

fun path2route(path: String): String = when {
    path == "/" -> HOME
    path.startsWith(HOME) -> path
    else -> "$HOME/$path"
}

fun route2path(route: String): String = when {
    route.startsWith("$HOME/") -> route.substringAfter("$HOME/")
    else -> route
}

fun getCurrentPath(): String = location.href
    .replace(location.origin, "")
    .replace("/#", "")

fun setTitle(title: String) {
    window.document.title = title
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

fun NavController.getRoute(): String {
    val segments = mutableListOf<String>()
    var nc: NavController? = this
    while (nc != null) {
        nc.getCurrentNavEntry()?.toPath()?.let { segments.add(0, it) }
        segments.add(0, "nc?${nc.key}")
        nc = nc.parent
    }
    return segments.joinToString("/")
}

private fun <Args> NavEntry<Args>.toPath(): String? {
    val ext = destination.ext<ScreenInfo<Args>>() ?: return null
    val name = ext.name ?: destination.name
    val args = navArgs?.let(ext.argsToString)
    return if (args == null) name else "$name?$args"
}

@JsName("window")
external val window: Window

@JsName("history")
external val history: History

@JsName("location")
external val location: Location

external class Window {
    val document: Document
    fun addEventListener(type: String, listener: (Any) -> Unit)
}

external class Document {
    var title: String
}

external class History {
    fun pushState(state: Any?, title: String, url: String?)
    fun replaceState(state: Any?, title: String, url: String?)
    val state: Any?
}

external class Location {
    val href: String
    val origin: String
}

external fun String.toJsString(): dynamic

private fun heroRoute(): String = "$HOME/"
