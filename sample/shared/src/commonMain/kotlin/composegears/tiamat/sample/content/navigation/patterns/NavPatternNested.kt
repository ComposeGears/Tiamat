package composegears.tiamat.sample.content.navigation.patterns

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
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

val NavPatternNested by navDestination(ScreenInfo()) {
    Screen("Nested navigation") {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            val content1 = remember {
                movableContentOf { modifier: Modifier ->
                    ItemContent("Group 1 nav controller", modifier)
                }
            }
            val content2 = remember {
                movableContentOf { modifier: Modifier ->
                    ItemContent("Group 2 nav controller", modifier)
                }
            }
            if (maxWidth > maxHeight) Row {
                content1(Modifier.weight(1f))
                HSpacer()
                content2(Modifier.weight(1f))
            } else Column {
                content1(Modifier.weight(1f))
                VSpacer()
                content2(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ItemContent(
    group: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = group,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        val nc = rememberNavController(
            key = group,
            startDestination = NavPatternNestedScreen1,
        )
        Navigation(
            navController = nc,
            destinations = arrayOf(
                NavPatternNestedScreen1,
                NavPatternNestedScreen2,
                NavPatternNestedScreen3
            )
        )
    }
}

private val NavPatternNestedScreen1 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.KeyboardArrowRight,
                onClick = { nc.navigate(NavPatternNestedScreen2) }
            )
        }
    }
}

private val NavPatternNestedScreen2 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    onClick = { nc.navigate(NavPatternNestedScreen3) }
                )
            }
        }
    }
}

private val NavPatternNestedScreen3 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
private fun NavPatternNestedPreview() = AppTheme {
    TiamatPreview(destination = NavPatternNested)
}
