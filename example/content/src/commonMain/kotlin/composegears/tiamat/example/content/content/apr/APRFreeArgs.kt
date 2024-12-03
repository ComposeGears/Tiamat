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
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import composegears.tiamat.example.ui.core.AppButton
import composegears.tiamat.example.ui.core.Screen
import composegears.tiamat.example.ui.core.VSpacer

val APRFreeArgs by navDestination<Unit> {
    Screen("FreeArgs") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "FreeArgs nav controller",
                startDestination = APRFreeArgsScreen1,
                destinations = arrayOf(
                    APRFreeArgsScreen1,
                    APRFreeArgsScreen2,
                )
            )
            Navigation(
                nc,
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

private val APRFreeArgsScreen1 by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Text(
                "`FreeArgs` is a free form(type) data\n" +
                    "Expect to be used as intent/call-to-action/meta-data\n" +
                    "You can clear freeArgs after processing."
            )
            VSpacer()
            Text("Click button to pass free-args value to next screen")
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

private val APRFreeArgsScreen2 by navDestination<Unit> {
    val nc = navController()
    val args = freeArgs<Any?>()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AnimatedContent(args, Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (it != null) {
                        Text("Type: ${it::class.simpleName}\nFreeArgs value: $it")
                        VSpacer()
                        AppButton(
                            "Clear",
                            onClick = { clearFreeArgs() }
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

private data class SomeFreeArgsDataClass(val t: Int)