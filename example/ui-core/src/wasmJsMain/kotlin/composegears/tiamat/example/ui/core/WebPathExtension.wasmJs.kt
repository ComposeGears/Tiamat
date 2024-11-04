package composegears.tiamat.example.ui.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.composegears.tiamat.*
import kotlinx.browser.window

actual fun <T> webPathExtension(
    argsToPathTransform: (T) -> String?,
    pathToArgsTransform: (String) -> T?
): Extension<T>? = WebPathExtension(argsToPathTransform, pathToArgsTransform)

//this is !!!VERY!!! simple & primitive way to handle web-navigation
class WebPathExtension<T>(
    private val argsToPathTransform: (T) -> String?,
    private val pathToArgsTransform: (String) -> T?
) : Extension<T>() {

    private fun <T> NavEntry<T>.toPath(): String? {
        val pathExt = destination.ext<WebPathExtension<T>>() ?: return null
        val name = destination.name
        val args = navArgs?.let { pathExt.argsToPathTransform(it) }
        return if (args == null) name else "$name?$args"
    }

    // should only be called for app start destination on page loaded
    fun navigate(navController: NavController, path: String) {
        val parts = path.split("?")
        val name = parts[0]
        val args = parts.getOrNull(1)
        val dest = navController.findDestination { it.name == name } ?: return
        paresArgsAndNavigate(navController, dest, args)

    }

    private fun <T> paresArgsAndNavigate(
        navController: NavController,
        destination: NavDestination<T>,
        argsStr: String?
    ) {
        print("paresArgsAndNavigate: $argsStr")
        val argsValue =
            if (argsStr == null) null
            else destination.ext<WebPathExtension<T>>()?.pathToArgsTransform?.invoke(argsStr)
        if (navController.current != destination) navController.navigate(destination, argsValue)
    }

    @Composable
    override fun NavDestinationScope<T>.content() {
        val entry = navEntry()
        LaunchedEffect(Unit) {
            window.history.replaceState(null, "", "./#" + entry.toPath())
        }
    }
}