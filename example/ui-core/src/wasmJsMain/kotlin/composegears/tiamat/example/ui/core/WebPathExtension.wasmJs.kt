package composegears.tiamat.example.ui.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.composegears.tiamat.*
import kotlinx.browser.window

actual fun <T> webPathExtension(
    path: String?,
    argsToPathTransform: (T) -> String?
): Extension<T>? = PathExtension(path, argsToPathTransform)

class PathExtension<T>(
    private val path: String?,
    private val argsToPathTransform: (T) -> String?
) : Extension<T>() {

    private fun <T> NavEntry<T>.toPath(): String? {
        val pathExt = destination.ext<PathExtension<T>>() ?: return null
        val name = pathExt.path ?: destination.name
        val args = navArgs?.let { pathExt.argsToPathTransform(it) }
        return if (args == null) name else "$name?$args"
    }

    @Composable
    override fun NavDestinationScope<T>.content() {
        val entry = navEntry()
        LaunchedEffect(Unit) {
            window.history.replaceState(null, "", entry.toPath())
        }
    }
}