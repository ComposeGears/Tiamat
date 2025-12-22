package composegears.tiamat.sample.content.navigation.data

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import composegears.tiamat.sample.icons.Icons
import composegears.tiamat.sample.icons.KeyboardArrowLeft
import composegears.tiamat.sample.icons.KeyboardArrowRight
import composegears.tiamat.sample.ui.*
import kotlinx.serialization.Serializable

val NavDataResult by navDestination(ScreenInfo()) {
    Screen("NavResult") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "NavResult nav controller",
                startDestination = NavDataResultScreen1,
            )
            Navigation(
                navController = nc,
                destinations = arrayOf(
                    NavDataResultScreen1,
                    NavDataResultScreen2,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

private val NavDataResultScreen1 by navDestination {
    val nc = navController()
    val result = navResult<Any>()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Text(
                text = "`NavDataResult` is a data passed back to this screen\n" +
                    "You can clear navResult after processing.\n" +
                    "navResult is NOT A STATE, clearing it will NOT cause recomposition",
                textAlign = TextAlign.Center
            )
            VSpacer()
            AnimatedContent(result, Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (it != null) {
                        Text("Type: ${it::class.simpleName}\nResult value: $it")
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
                endIcon = Icons.KeyboardArrowRight,
                onClick = { nc.navigate(NavDataResultScreen2) }
            )
        }
    }
}

private val NavDataResultScreen2 by navDestination<Int> {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back (Result is `String`)",
                modifier = Modifier.widthIn(min = 400.dp),
                startIcon = Icons.KeyboardArrowLeft,
                onClick = { nc.back(result = "Some String") }
            )
            AppButton(
                "Back (Result is `Int`)",
                modifier = Modifier.widthIn(min = 400.dp),
                startIcon = Icons.KeyboardArrowLeft,
                onClick = { nc.back(result = 1) }
            )
            AppButton(
                "Back (Result is `Class`)",
                modifier = Modifier.widthIn(min = 400.dp),
                startIcon = Icons.KeyboardArrowLeft,
                onClick = { nc.back(result = SomeNavDataResultDataClass(1)) }
            )
            AppButton(
                "Back (Result is nothing)",
                modifier = Modifier.widthIn(min = 400.dp),
                startIcon = Icons.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

@Serializable // serialization is optional
private data class SomeNavDataResultDataClass(val t: Int)

@Preview
@Composable
private fun NavDataResultPreview() = AppTheme {
    TiamatPreview(destination = NavDataResult)
}
