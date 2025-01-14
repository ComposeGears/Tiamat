package composegears.tiamat.example.content.content.architecture

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composegears.tiamat.*
import composegears.tiamat.example.ui.core.*

val ArchCustomSaveState by navDestination<Unit>(ScreenInfo()) {
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
                    destinations = arrayOf(
                        ArchCustomSaveStateScreen1,
                        ArchCustomSaveStateScreen2,
                        ArchCustomSaveStateScreen3,
                    ),
                    configuration = { ncSavedState?.let(::loadFromSavedState) }
                )
                Navigation(
                    nc,
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                )
                DisposableEffect(Unit) {
                    onDispose {
                        ncSavedState = nc.getSavedState()
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

private val ArchCustomSaveStateScreen1 by navDestination<Unit> {
    val nc = navController()
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen 1", style = MaterialTheme.typography.headlineMedium)
            VSpacer()
            AppButton(
                "Next",
                endIcon = Icons.AutoMirrored.Default.KeyboardArrowRight,
                onClick = { nc.navigate(ArchCustomSaveStateScreen2) }
            )
        }
    }
}

private val ArchCustomSaveStateScreen2 by navDestination<Unit> {
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
                    onClick = { nc.navigate(ArchCustomSaveStateScreen3) }
                )
            }
        }
    }
}

private val ArchCustomSaveStateScreen3 by navDestination<Unit> {
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