package composegears.tiamat.example.content.content.apr

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import composegears.tiamat.example.ui.core.AppButton
import composegears.tiamat.example.ui.core.Screen
import composegears.tiamat.example.ui.core.ScreenInfo
import composegears.tiamat.example.ui.core.VSpacer

val APRNavResult by navDestination<Unit>(ScreenInfo()) {
    Screen("NavResult") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "NavResult nav controller",
                startDestination = APRNavResultScreen1,
            )
            Navigation(
                navController = nc,
                destinations = arrayOf(
                    APRNavResultScreen1,
                    APRNavResultScreen2,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

private val APRNavResultScreen1 by navDestination<Unit> {
    val nc = navController()
    val result = navResult<Any?>()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Text(
                text = "`NavResult` is a data passed back to this screen\n" +
                    "You can clear navResult after processing.",
                textAlign = TextAlign.Center
            )
            VSpacer()
            AnimatedContent(result, Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (it != null) {
                        Text("Type: ${it::class.simpleName}\nResult value: $it")
                        VSpacer()
                        AppButton(
                            "Clear",
                            onClick = { clearNavResult() } // todo indicate it was actually cleared
                        )
                    } else {
                        Text(
                            text = "Click button and select the result to be returned to this screen",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(APRNavResultScreen2) }
            )
        }
    }
}

private val APRNavResultScreen2 by navDestination<Int> {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back (Result is `String`)",
                modifier = Modifier.widthIn(min = 400.dp),
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back(result = "Some String") }
            )
            AppButton(
                "Back (Result is `Int`)",
                modifier = Modifier.widthIn(min = 400.dp),
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back(result = 1) }
            )
            AppButton(
                "Back (Result is `Class`)",
                modifier = Modifier.widthIn(min = 400.dp),
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back(result = SomeNavResultDataClass(1)) }
            )
            AppButton(
                "Back (Result is nothing)",
                modifier = Modifier.widthIn(min = 400.dp),
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

private data class SomeNavResultDataClass(val t: Int)