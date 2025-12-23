package composegears.tiamat.sample

import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.navigation.NavController
import com.composegears.tiamat.navigation.NavEntry
import com.composegears.tiamat.navigation.Route
import kotlinx.browser.window
import org.w3c.dom.PopStateEvent

private const val TITLE = "Tiamat Wasm"

internal external fun encodeURIComponent(str: String): String
internal external fun decodeURIComponent(encodedURI: String): String
internal external fun addEventListener(type: String, callback: (PopStateEvent) -> Unit)

@OptIn(ExperimentalWasmJsInterop::class, TiamatExperimentalApi::class)
internal object Browser {

    fun bind(
        navController: NavController,
    ) {
        addEventListener("popstate") { event ->
            println("event: $event")
            flushLocation(navController)
        }
        navController.setOnNavigationListener { _, to, _ ->
            println("navigated to: $to")
            updateHistory(navController)
            window.document.title = titleOf(navController.getCurrentNavEntry())

        }
        flushLocation(navController)
        window.document.title = titleOf(navController.getCurrentNavEntry())
        updateHistory(navController, forceReplace = true)
    }

    fun titleOf(entry: NavEntry<*>?): String {
        val name = entry?.destination?.name.orEmpty()
        return if (name.isEmpty()) TITLE else "$TITLE / $name"
    }

    fun getCurrentPath(): String = window.location.href.replace(window.location.origin, "")

    fun flushLocation(
        navController: NavController,
    ) {
        val path = getCurrentPath().takeIf { it != "/" }
        println("applyState->path: $path")
        path?.let(::path2route)?.let(navController::route)
    }

    fun updateHistory(
        navController: NavController,
        forceReplace: Boolean = false,
    ) {
        println("applyHistory: ${navController2path(navController)} | currentPath=${getCurrentPath()}")
        val title = window.document.title
        val path = navController2path(navController)
        if (forceReplace) {
            window.history.replaceState(data = null, title = title, url = path)
        } else {
            window.history.pushState(data = null, title = title, url = path)
        }
    }

    fun path2route(path: String): Route {
        println("path2route: $path")
        val segments = path.removePrefix("/").removePrefix("#/").split('/').filter { it.isNotBlank() }
        println("segments: $segments")
        return Route {
            segments.onEach {
                destination(decodeURIComponent(it))
            }
        }
    }

    fun navController2path(navController: NavController): String {
        val stack = navController.getNavStack()
        if (stack.isEmpty()) return "/"
        return stack.joinToString(separator = "/", prefix = "/#/") { encodeURIComponent(it.destination.name) }
    }
}
