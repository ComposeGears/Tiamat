package composegears.tiamat.sample.content.navigation

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.NavDestination
import composegears.tiamat.sample.ui.*

val NavForwardAndBack by navDestination(ScreenInfo()) {
    Screen("Forward & back") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "F&B nav controller",
                startDestination = NavForwardAndBackScreen1,
            )
            Navigation(
                navController = nc,
                destinations = arrayOf(
                    NavForwardAndBackScreen1,
                    NavForwardAndBackScreen2,
                    NavForwardAndBackScreen3,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

// we define type here to bypass circular initialization issue
private val NavForwardAndBackScreen1: NavDestination<Unit> by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(NavForwardAndBackScreen2) }
            )
        }
    }
}

private val NavForwardAndBackScreen2 by navDestination {
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
                    onClick = { nc.navigate(NavForwardAndBackScreen3) }
                )
            }
        }
    }
}

private val NavForwardAndBackScreen3 by navDestination {
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
            AppButton(
                "Back to \"Screen 1\"",
                startIcon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                onClick = { nc.back(to = NavForwardAndBackScreen1) }
            )
        }
    }
}

@Preview
@Composable
private fun NavForwardAndBackPreview() = AppTheme {
    TiamatPreview(destination = NavForwardAndBack)
}