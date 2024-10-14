package composegears.tiamat.example.extensions

import com.composegears.tiamat.Extension

expect class PathExtension<T>(
    path: String? = null,
    argsToPathTransform: (T) -> String? = { null }
) : Extension<T>
