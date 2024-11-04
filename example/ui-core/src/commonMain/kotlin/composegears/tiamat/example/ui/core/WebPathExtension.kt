package composegears.tiamat.example.ui.core

import com.composegears.tiamat.Extension

expect fun <T> webPathExtension(
    argsToPathTransform: (T) -> String? = { null },
    pathToArgsTransform: (String) -> T? = { null }
): Extension<T>?