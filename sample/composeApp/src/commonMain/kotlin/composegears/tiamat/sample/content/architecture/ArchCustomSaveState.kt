package composegears.tiamat.sample.content.architecture

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.compose.*
import com.composegears.tiamat.navigation.SavedState
import com.composegears.tiamat.toHumanReadableString
import composegears.tiamat.sample.icons.Icons
import composegears.tiamat.sample.icons.KeyboardArrowLeft
import composegears.tiamat.sample.icons.KeyboardArrowRight
import composegears.tiamat.sample.ui.*

val ArchCustomSaveState by navDestination(ScreenInfo()) {
    Screen("Custom SaveState") {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            var ncSavedState by remember { mutableStateOf<SavedState?>(null) }
            var showNavigation by remember { mutableStateOf(true) }
            VSpacer()
            AnimatedContent(showNavigation) {
                if (it) AppButton("Save", onClick = { showNavigation = false })
                else AppButton("Restore", onClick = { showNavigation = true })
            }
            if (showNavigation) {
                val nc = rememberNavController(
                    key = "Arch custom save state nav controller",
                    startDestination = ArchCustomSaveStateScreen1,
                    savedState = ncSavedState,
                )
                Navigation(
                    navController = nc,
                    destinations = arrayOf(
                        ArchCustomSaveStateScreen1,
                        ArchCustomSaveStateScreen2,
                        ArchCustomSaveStateScreen3,
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                )
                DisposableEffect(Unit) {
                    onDispose {
                        ncSavedState = nc.saveToSavedState()
                    }
                }
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        "Nav controller state:\n\n${ncSavedState?.toHumanReadableString()}",
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

private val ArchCustomSaveStateScreen1 by navDestination {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.KeyboardArrowRight,
                onClick = { nc.navigate(ArchCustomSaveStateScreen2) }
            )
        }
    }
}

private val ArchCustomSaveStateScreen2 by navDestination {
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
                    onClick = { nc.navigate(ArchCustomSaveStateScreen3) }
                )
            }
        }
    }
}

private val ArchCustomSaveStateScreen3 by navDestination {
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
private fun ArchCustomSaveStatePreview() = AppTheme {
    TiamatPreview(destination = ArchCustomSaveState)
}
