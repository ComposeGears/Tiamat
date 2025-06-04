package composegears.tiamat.example.ui.core

import com.composegears.tiamat.compose.NavExtension

/**
 * Screen information extension.
 *
 * @property name The name of the screen.
 * @property srcReference The source reference of the screen.
 * @property argsToString An arguments serializer.
 * @property stringToArgs An arguments deserializer.
 */
class ScreenInfo<T>(
    val name: String? = null,
    val srcReference: String? = null,
    val argsToString: (T) -> String? = { null },
    val stringToArgs: (String?) -> T? = { null },
) : NavExtension<T>