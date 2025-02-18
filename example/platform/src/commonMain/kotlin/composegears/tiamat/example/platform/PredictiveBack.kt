package composegears.tiamat.example.platform

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import composegears.tiamat.example.ui.core.*

@Composable
internal expect fun PredictiveBackContainer(
    navController: NavController,
    enabled: Boolean,
    modifier: Modifier,
    content: @Composable () -> Unit
)

val PredictiveBack by navDestination<Unit>(ScreenInfo()) {
    Screen("PredictiveBack") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "PredictiveBack nav controller",
                startDestination = PredictiveBackScreen1,
                destinations = arrayOf(
                    PredictiveBackScreen1,
                    PredictiveBackScreen2,
                    PredictiveBackScreen3,
                )
            )
            PredictiveBackContainer(
                navController = nc,
                enabled = true,
                modifier = Modifier.fillMaxSize()
            ) {
                Navigation(
                    nc,
                    Modifier.fillMaxSize()
                )
            }
        }
    }
}

private val PredictiveBackScreen1 by navDestination<Unit> {
    val nc = navController()
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(PredictiveBackScreen2) }
            )
        }
    }
}

private val PredictiveBackScreen2 by navDestination<Unit> {
    val nc = navController()
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
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
                    onClick = { nc.navigate(PredictiveBackScreen3) }
                )
            }
        }
    }
}

private val PredictiveBackScreen3 by navDestination<Unit> {
    val nc = navController()
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
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