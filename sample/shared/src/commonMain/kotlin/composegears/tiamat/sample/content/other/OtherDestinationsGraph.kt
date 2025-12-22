@file:OptIn(TiamatExperimentalApi::class)

package composegears.tiamat.sample.content.other

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.destinations.InstallIn
import com.composegears.tiamat.destinations.TiamatGraph
import composegears.tiamat.sample.icons.Icons
import composegears.tiamat.sample.icons.KeyboardArrowLeft
import composegears.tiamat.sample.icons.KeyboardArrowRight
import composegears.tiamat.sample.ui.*

private object Graph : TiamatGraph

val OtherDestinationsGraph by navDestination(ScreenInfo()) {
    Screen("Auto destinations graph") {
        val nc = rememberNavController(
            key = "Auto-destinations nav controller",
            startDestination = OtherDestinationsGraphScreen1,
        )
        Navigation(
            navController = nc,
            graph = Graph,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
        )
    }
}

@InstallIn(Graph::class)
private val OtherDestinationsGraphScreen1 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.KeyboardArrowRight,
                onClick = { nc.navigate(OtherDestinationsGraphScreen2) }
            )
        }
    }
}

@InstallIn(Graph::class)
private val OtherDestinationsGraphScreen2 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Row {
                AppButton(
                    "Back",
                    startIcon = Icons.KeyboardArrowLeft,
                    onClick = { nc.back() }
                )
                HSpacer()
                AppButton(
                    "Next",
                    endIcon = Icons.KeyboardArrowRight,
                    onClick = { nc.navigate(OtherDestinationsGraphScreen3) }
                )
            }
        }
    }
}

@InstallIn(Graph::class)
private val OtherDestinationsGraphScreen3 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 3", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                startIcon = Icons.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}

@Preview
@Composable
private fun OtherDestinationsGraphPreview() = AppTheme {
    TiamatPreview(destination = OtherDestinationsGraph)
}
