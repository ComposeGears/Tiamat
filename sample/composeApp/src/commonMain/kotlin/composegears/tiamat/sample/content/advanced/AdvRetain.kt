package composegears.tiamat.sample.content.advanced

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.compose.*
import composegears.tiamat.sample.icons.Icons
import composegears.tiamat.sample.icons.KeyboardArrowLeft
import composegears.tiamat.sample.icons.KeyboardArrowRight
import composegears.tiamat.sample.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

val AdvRetain by navDestination(ScreenInfo()) {
    Screen("Retain") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "Retain nav controller",
                startDestination = AdvRetainScreen1,
            )
            Navigation(
                navController = nc,
                destinations = arrayOf(
                    AdvRetainScreen1,
                    AdvRetainScreen2,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

@OptIn(TiamatExperimentalApi::class)
private val AdvRetainScreen1 by navDestination {
    val nc = navController()
    val retainedData = retain { RTData() }
    val rememberedData = remember { RTData() }

    val producedState by produceState(0) {
        while (isActive) {
            delay(1000)
            value++
        }
    }
    val retainedState by produceRetainedState(0) {
        while (isActive) {
            delay(1000)
            value++
        }
    }

    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Text(
                text = "Retained value: $retainedData",
                textAlign = TextAlign.Center
            )
            Text(
                text = "Remembered value: $rememberedData",
                textAlign = TextAlign.Center
            )
            Text("Produced State: $producedState", textAlign = TextAlign.Center)
            Text("Retained State: $retainedState", textAlign = TextAlign.Center)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.KeyboardArrowRight,
                onClick = { nc.navigate(AdvRetainScreen2) }
            )
        }
    }
}
private val AdvRetainScreen2 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                startIcon = Icons.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

private data class RTData(val id: Int = Random.nextInt())

@Preview
@Composable
private fun AdvRetainPreview() = AppTheme {
    TiamatPreview(destination = AdvRetain)
}
