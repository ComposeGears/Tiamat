package composegears.tiamat.sample.content.navigation

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

val NavReplace by navDestination(ScreenInfo()) {
    Screen("Replace") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val nc = rememberNavController(
                key = "Replace nav controller",
                startDestination = NavReplaceScreen1,
            )
            Navigation(
                navController = nc,
                destinations = arrayOf(
                    NavReplaceScreen1,
                    NavReplaceScreen2,
                    NavReplaceScreen3,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

private val NavReplaceScreen1 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.KeyboardArrowRight,
                onClick = { nc.navigate(NavReplaceScreen2) }
            )
        }
    }
}

private val NavReplaceScreen2 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 2", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Click replace button to replace current screen",
                textAlign = TextAlign.Center
            )
            Text(
                text = "it will not be placed at back stack",
                textAlign = TextAlign.Center
            )
            VSpacer()
            Row {
                AppButton(
                    "Back",
                    startIcon = Icons.KeyboardArrowLeft,
                    onClick = { nc.back() }
                )
                HSpacer()
                AppButton(
                    "Replace",
                    endIcon = Icons.KeyboardArrowRight,
                    onClick = { nc.replace(NavReplaceScreen3) }
                )
            }
        }
    }
}

private val NavReplaceScreen3 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 3", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = """Click "Back" to see "Screen 1"""",
                textAlign = TextAlign.Center
            )
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
private fun NavReplacePreview() = AppTheme {
    TiamatPreview(destination = NavReplace)
}
