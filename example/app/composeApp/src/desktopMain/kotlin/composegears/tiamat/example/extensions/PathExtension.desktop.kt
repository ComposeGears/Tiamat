package composegears.tiamat.example.extensions

import androidx.compose.runtime.Composable
import com.composegears.tiamat.Extension
import com.composegears.tiamat.NavDestinationScope

actual class PathExtension<T> actual constructor(
    path: String?,
    argsToPathTransform: (T) -> String?
) : Extension<T>() {
    @Composable
    override fun NavDestinationScope<T>.content() = Unit
}