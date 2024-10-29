package composegears.tiamat.example.ui.core

import com.composegears.tiamat.Extension

expect fun <T> webPathExtension(
    path: String? = null,
    argsToPathTransform: (T) -> String? = { null }
): Extension<T>?