package composegears.tiamat.example.ui.core

import com.composegears.tiamat.Extension

actual fun <T> webPathExtension(
    argsToPathTransform: (T) -> String?,
    pathToArgsTransform: (String) -> T?
): Extension<T>? = null