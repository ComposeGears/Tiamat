@file:OptIn(TiamatExperimentalApi::class)

package composegears.tiamat.example.content.content.advanced

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
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.destinations.InstallIn
import com.composegears.tiamat.destinations.TiamatGraph
import composegears.tiamat.example.ui.core.*

private object Graph : TiamatGraph

val AdvDestinationsGraph by navDestination<Unit>(ScreenInfo()) {
    Screen("Auto destinations graph") {
        val nc = rememberNavController(
            key = "Auto-destinations nav controller",
            startDestination = AdvDestinationsGraphScreen1,
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
private val AdvDestinationsGraphScreen1 by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(AdvDestinationsGraphScreen2) }
            )
        }
    }
}

@InstallIn(Graph::class)
private val AdvDestinationsGraphScreen2 by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            Row {
                AppButton(
                    "Back",
                    startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    onClick = { nc.back() }
                )
                HSpacer()
                AppButton(
                    "Next",
                    endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    onClick = { nc.navigate(AdvDestinationsGraphScreen3) }
                )
            }
        }
    }
}

@InstallIn(Graph::class)
private val AdvDestinationsGraphScreen3 by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 3", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Back",
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back() }
            )
        }
    }
}