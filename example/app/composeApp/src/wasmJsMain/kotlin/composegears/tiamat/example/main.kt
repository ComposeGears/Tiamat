@file:Suppress("Filename")

package composegears.tiamat.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import composegears.tiamat.example.content.App
import composegears.tiamat.example.platform.Platform

external fun onLoadFinished()

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    Platform.start()
    ComposeViewport(viewportContainerId = "TiamatTarget") {
        LaunchedEffect(Unit) {
            onLoadFinished()
        }
        Box {
            App()
            Text(
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                text = "Wasm Alpha",
            )
        }
    }
}

/*
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    KoinLib.start()
    val path = window.location.href
        .replace(window.location.origin, "")
        .replace("/#", "")
    ComposeViewport(viewportContainerId = "TiamatTarget") {
        LaunchedEffect(Unit) {
            onLoadFinished()
        }
        Box {
            App(
                controllerConfig = { nc ->
                    // this is !!!VERY!!! simple & primitive way to handle web-navigation
                    // let main screen's ext to navigate if needed
                    nc.current?.ext<WebPathExtension<*>>()?.navigate(nc, path)
                },
            )
            Text(
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                text = "Wasm Alpha",
            )
        }
    }
}*/
