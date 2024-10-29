package composegears.tiamat.example.ui.core

import com.composegears.tiamat.Extension

actual fun <T> webPathExtension(
    path: String?,
    argsToPathTransform: (T) -> String?
): Extension<T>? = null