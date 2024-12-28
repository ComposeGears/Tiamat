@file:Suppress("FunctionName")

package composegears.tiamat.example.platform

import androidx.compose.runtime.Composable
import com.composegears.tiamat.*

// todo add some explanations on what the hell is going on here

// ------------- Ext builder & helper-----------

val DestinationPathExt: Extension<Any?> by lazy { DestinationPathExt() }

fun <Args> DestinationPathExt(
    name: String? = null,
    argsToString: (Args) -> String? = { null },
    stringToArgs: (String) -> Args? = { null },
    backStackDestination: () -> List<NavDestination<*>>
): Extension<Args> = DestinationPathExtImpl(
    name = name,
    argsToString = argsToString,
    stringToArgs = stringToArgs,
    backStackItems = { backStackDestination().map { it.toNavEntry() } }
)

fun <Args> DestinationPathExt(
    name: String? = null,
    argsToString: (Args) -> String? = { null },
    stringToArgs: (String) -> Args? = { null },
    backStackEntries: (Args?) -> List<NavEntry<*>> = { emptyList() }
): Extension<Args> = DestinationPathExtImpl(
    name = name,
    argsToString = argsToString,
    stringToArgs = stringToArgs,
    backStackItems = backStackEntries
)

// -------------- Utils ----------------------

private fun <Args> NavEntry<Args>.toPath(): String? {
    val ext = destination.ext<DestinationPathExtImpl<Args>>() ?: return null
    val name = ext.name ?: destination.name
    val args = navArgs?.let(ext.argsToString)
    return if (args == null) name else "$name?$args"
}

private fun <Args> NavDestination<Args>.toEntries(argsStr: String?): List<NavEntry<*>> {
    val ext = ext<DestinationPathExtImpl<Args>>() ?: error("DestinationPathExt not found")
    val args = argsStr?.let(ext.stringToArgs)
    val backStackItems = ext.backStackItems(args)
    return backStackItems + toNavEntry(args)
}

fun NavController.getDestinationPath(): String {
    val segments = mutableListOf<String>()
    var nc: NavController? = this
    while (nc != null) {
        val localSegments = mutableListOf<String>()
        nc.getBackStack().forEach { it.toPath()?.let { p -> localSegments.add(p) } }
        nc.currentNavEntry?.toPath()?.let { localSegments.add(it) }
        segments.addAll(0, localSegments)
        nc = nc.parent
    }
    return segments.joinToString("/")
}

@OptIn(TiamatExperimentalApi::class)
fun NavController.openDestinationPath(path: String) {
    route(
        Route.build(forceReplace = true) {
            path.split("/").forEach { segment ->
                val targetName = segment.substringBefore("?")
                val argsStr = segment.substringAfter("?", "").takeIf { it.isNotBlank() }
                routeList(
                    description = "Follow: $segment",
                    entriesProvider = { nc ->
                        val destination = nc.findDestination {
                            val ext = it.ext<DestinationPathExtImpl<*>>() ?: return@findDestination false
                            val name = ext.name ?: it.name
                            name == targetName
                        } ?: return@routeList null
                        destination.toEntries(argsStr)
                    }
                )
            }
        }
    )
}

// ---------------- Extensions -----------------

internal class DestinationPathExtImpl<Args>(
    val name: String?,
    val argsToString: (Args) -> String?,
    val stringToArgs: (String) -> Args?,
    val backStackItems: (Args?) -> List<NavEntry<*>> = { emptyList() }
) : Extension<Args> {
    @Composable
    override fun NavDestinationScope<out Args>.Content() {
    }
}