@file:Suppress("MissingPackageDeclaration")

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

external fun onLoadFinished()

@Composable
fun PageLoadNotify() {
    LaunchedEffect(Unit) {
        onLoadFinished()
    }
}