package composegears.tiamat.sample.content.apr

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import composegears.tiamat.sample.ui.AppButton
import composegears.tiamat.sample.ui.AppTheme
import composegears.tiamat.sample.ui.Screen
import composegears.tiamat.sample.ui.ScreenInfo
import composegears.tiamat.sample.ui.VSpacer
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview

val APRFreeArgs by navDestination(ScreenInfo()) {
    Screen("FreeArgs") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "FreeArgs nav controller",
                startDestination = APRFreeArgsScreen1,
            )
            Navigation(
                navController = nc,
                destinations = arrayOf(
                    APRFreeArgsScreen1,
                    APRFreeArgsScreen2,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

private val APRFreeArgsScreen1 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Text(
                text = """
                    `FreeArgs` is a free form(type) data
                     Expect to be used as intent/call-to-action/meta-data
                     You can clear freeArgs after processing.
                     freeArgs is NOT A STATE, clearing it will NOT cause recomposition
                """.trimIndent(),
                textAlign = TextAlign.Center
            )
            VSpacer()
            Text(
                text = "Click button to pass free-args value to next screen",
                textAlign = TextAlign.Center
            )
            VSpacer()
            AppButton(
                "Next (Pass `String`)",
                modifier = Modifier.widthIn(min = 400.dp),
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(APRFreeArgsScreen2, freeArgs = "Some String") }
            )
            AppButton(
                "Next (Pass `Int`)",
                modifier = Modifier.widthIn(min = 400.dp),
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(APRFreeArgsScreen2, freeArgs = 1) }
            )
            AppButton(
                "Next (Pass `Class`)",
                modifier = Modifier.widthIn(min = 400.dp),
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(APRFreeArgsScreen2, freeArgs = SomeFreeArgsDataClass(1)) }
            )
            AppButton(
                "Next (Pass nothing)",
                modifier = Modifier.widthIn(min = 400.dp),
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(APRFreeArgsScreen2) }
            )
        }
    }
}

private val APRFreeArgsScreen2 by navDestination {
    val nc = navController()
    var args = freeArgs<Any>()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AnimatedContent(args, Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (it != null) {
                        Text(
                            text = "Type: ${it::class.simpleName}\nFreeArgs value: $it",
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text("FreeArgs is empty")
                    }
                }
            }
            VSpacer()
            AppButton(
                "Back",
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

@Serializable
private data class SomeFreeArgsDataClass(val t: Int)

@Preview
@Composable
private fun APRFreeArgsPreview() = AppTheme {
    TiamatDestinationPreview(destination = APRFreeArgs)
}
