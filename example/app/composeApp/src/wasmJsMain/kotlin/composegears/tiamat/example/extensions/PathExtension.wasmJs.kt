package composegears.tiamat.example.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.composegears.tiamat.*
import kotlinx.browser.window

actual class PathExtension<T> actual constructor(
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