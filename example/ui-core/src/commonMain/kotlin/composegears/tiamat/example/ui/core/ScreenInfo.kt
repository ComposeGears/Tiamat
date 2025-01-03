package composegears.tiamat.example.ui.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import com.composegears.tiamat.*

typealias ScreenOpenListener = (NavController, NavDestination<*>) -> Unit

val LocalScreenHandler = staticCompositionLocalOf { ScreenHandler() }

class ScreenHandler internal constructor() {
    private val listeners = mutableListOf<ScreenOpenListener>()

    fun addScreenOpenListener(listener: ScreenOpenListener): ScreenOpenListener {
        listeners.add(listener)
        return listener
    }

    fun removeScreenOpenListener(listener: ScreenOpenListener): Boolean =
        listeners.remove(listener)

    internal fun notifyScreenOpen(navController: NavController, navDestination: NavDestination<*>) {
        listeners.onEach { it(navController, navDestination) }
    }
}

/**
 * Various screen info
 *
 * @param srcReference ref to github sources
 * @param argsToString args serializer
 * @param stringToArgs args deserializer
 */
class ScreenInfo<T>(
    val name: String? = null,
    val srcReference: String? = null,
    val argsToString: (T) -> String? = { null },
    val stringToArgs: (String?) -> T? = { null },
) : ContentExtension<T> {
    @Composable
    override fun NavDestinationScope<out T>.Content() {
        val screenChangedHandler = LocalScreenHandler.current
        val navController = navController()
        val navDestination = navEntry().destination
        LaunchedEffect(Unit) {
            screenChangedHandler.notifyScreenOpen(navController, navDestination)
        }
    }

    // Ext suppose to be underlay - so screenChangeHandler will be notified
    // in same order as screen layout order
    // eg:   notify this -> layout thus -> notify child opened-> child layout
    // with overlay it would be: layout this -> layout child -> notify child opened -> notify this opened
    override fun getType(): ContentExtension.Type = ContentExtension.Type.Underlay
}